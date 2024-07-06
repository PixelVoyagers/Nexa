package pixel.nexa.plugin.adapter.discord

import com.neovisionaries.ws.client.WebSocketFactory
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.jdabuilder.light
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.GenericMessageEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import okhttp3.OkHttpClient
import pixel.auxframework.component.factory.getComponent
import pixel.auxframework.core.registry.Identifier
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.NexaCore
import pixel.nexa.core.platform.adapter.AbstractNexaBot
import pixel.nexa.core.platform.getListenersOfType
import pixel.nexa.core.resource.asset.Languages
import pixel.nexa.core.util.ConstantUtils
import pixel.nexa.network.command.CommandAutoComplete
import pixel.nexa.network.command.CommandService
import pixel.nexa.network.command.NexaCommand
import pixel.nexa.network.entity.user.User
import pixel.nexa.network.message.GenericMessageEventHandler
import pixel.nexa.plugin.adapter.discord.DiscordUtils.putDiscordTranslations
import pixel.nexa.plugin.adapter.discord.DiscordUtils.toNexa
import pixel.nexa.plugin.adapter.discord.command.DiscordSlashCommandSession
import pixel.nexa.plugin.adapter.discord.command.verify
import pixel.nexa.plugin.adapter.discord.entity.DiscordUser
import pixel.nexa.plugin.adapter.discord.message.DiscordMessageSession
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.max
import kotlin.reflect.full.callSuspend

class DiscordBotListener(private val bot: DiscordBot) : ListenerAdapter() {

    private val componentFactory = bot.getAdapter().getContext().getAuxContext().componentFactory()
    private val nexaContext = bot.getAdapter().getContext()

    override fun onGenericMessage(event: GenericMessageEvent) =
        event.channel.retrieveMessageById(event.messageId).queue { message ->
            val messageSession = DiscordMessageSession(bot, event, message)
            messageSession.setLanguage(
                messageSession.getUser().getLanguageOrNull() ?: componentFactory.getComponent<Languages>().getDefault()
            )
            nexaContext.getListenersOfType<GenericMessageEventHandler>().forEach {
                it.handleGenericMessageEvent(messageSession)
            }
        }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val commands =
            componentFactory.getComponent<CommandService>()
                .getCommands()
        val command = commands.first {
            event.fullCommandName == it.getCommandData().getIdentifier()
                .format { namespace, path -> "$namespace-${path.split("/").joinToString("-")}" }
        }
        val session = DiscordSlashCommandSession(command, event, bot)
        runBlocking {
            command.getCommandData().getAction().invoke(session)
        }
    }

    override fun onCommandAutoCompleteInteraction(event: CommandAutoCompleteInteractionEvent) {
        val command =
            componentFactory.getComponent<CommandService>()
                .getCommands()
                .first {
                    event.fullCommandName == it.getCommandData().getIdentifier()
                        .format { namespace, path -> "$namespace-${path.split("/").joinToString("-")}" }
                }
        val result = mutableListOf<CommandAutoComplete.Choice>()
        val user = bot.cachePool.getOrPut(event.user.id) { event.verify(DiscordUser(bot, event.user)) }
        for (autoComplete in command.getCommandData().getAutoComplete()) {
            if (event.focusedOption.name in autoComplete.key) {
                val autoCompleteEvent =
                    CommandAutoComplete(
                        event.focusedOption.value,
                        event.focusedOption.name,
                        mutableListOf(),
                        user,
                        user.getLanguageOrNull() ?: event.userLocale.toNexa(bot.getAdapter().getContext())
                    )
                runBlocking {
                    autoComplete.value.callSuspend(command, autoCompleteEvent)
                }
                result += autoCompleteEvent.result
            }
        }
        val importantChoices = result.filter { it.important }
        fun match(input: Identifier, choice: Identifier) =
            input.getNamespace() in choice.getNamespace() && input.getPath() in choice.getPath()

        val commonChoices = result.filter { that ->
            event.focusedOption.value.let {
                if (it in that.display) return@let true
                if (it in that.value) return@let true
                else return@let runCatching {
                    match(
                        identifierOf(it, NexaCore.DEFAULT_NAMESPACE),
                        identifierOf(that.display, NexaCore.DEFAULT_NAMESPACE)
                    ) || match(
                        identifierOf(it, NexaCore.DEFAULT_NAMESPACE),
                        identifierOf(that.value, NexaCore.DEFAULT_NAMESPACE)
                    )
                }.getOrNull()
            } ?: false
        }.filterNot { it.important }
        val choices = mutableListOf<CommandAutoComplete.Choice>()
        choices += importantChoices.take(25)
        choices += commonChoices.take(max(25 - importantChoices.size, 0))
        event.replyChoices(
            choices.map {
                Command.Choice(it.display, it.value)
            }
        ).queue()
    }

}

class DiscordBotInternal(private val bot: DiscordBot) : AbstractNexaBot.Internal(bot) {

    override suspend fun getUserById(id: String) = suspendCoroutine { continuation ->
        bot.getInstance().retrieveUserById(id).queue {
            continuation.resume(bot.cachePool.getOrPut(it.id) { DiscordUser(bot, it) })
        }
    }

    override suspend fun getCacheUsers() = bot.cachePool.getAll<User>().values.toSet()

}

class DiscordBot(private val discordAdapter: DiscordAdapter, private val config: Map<String, Any>) :
    AbstractNexaBot<DiscordBot>() {

    private val internal = DiscordBotInternal(this)
    override fun internal() = internal

    val cachePool = DiscordCachePool()

    override fun getAdapter() = discordAdapter

    private lateinit var botInstance: JDA
    fun getInstance() = botInstance

    override fun getSelfId() = botInstance.selfUser.id

    fun login() = light(config["token"].toString(), enableCoroutines = true) {
        if ("proxy" in config) {
            val proxyUri = URI.create(config["proxy"].toString())
            setWebsocketFactory(WebSocketFactory().apply { proxySettings.setServer(proxyUri) })
            setHttpClientBuilder(
                OkHttpClient.Builder().proxy(
                    Proxy(
                        Proxy.Type.entries.first { it.name.lowercase() == proxyUri.scheme },
                        InetSocketAddress(proxyUri.host, proxyUri.port)
                    )
                )
            )
        }
        if ("intents" in config) enableIntents(GatewayIntent.getIntents(config["intents"].toString().toInt()))
        addEventListeners(DiscordBotListener(this@DiscordBot))
    }

    override fun start() {
        botInstance = login()
        botInstance.awaitReady()
        botInstance.updateCommands(this::updateCommands).queue()
    }

    fun updateCommands(action: CommandListUpdateAction) {
        val applicationContext = getAdapter().getContext().getAuxContext()
        val commands = applicationContext.componentFactory().getComponent<CommandService>()
        val discordCommands = mutableSetOf<CommandData>()
        for (command in commands.getCommands()) {
            val name = command.getCommandData().getIdentifier()
            val commandData = Commands.slash(
                name.format { namespace, path -> "$namespace-${path.split("/").joinToString("-")}" },
                ConstantUtils.EMPTY_COMMAND_DESCRIPTION
            )
            for (option in command.getCommandData().getOptions()) {
                commandData.addOption(
                    DiscordUtils.toDiscordOptionType(option.getType()),
                    option.getName(),
                    ConstantUtils.EMPTY_COMMAND_OPTION_DESCRIPTION,
                    option.isRequired(),
                    option.getAutoCompleteMode() != NexaCommand.Option.AutoCompleteMode.DISABLED
                )
            }
            command.getCommandData().getNexaCommand().also {
                it.commandTranslator.putDiscordTranslations(commandData, getAdapter().getContext())
            }
            discordCommands += commandData
        }
        action.addCommands(discordCommands)
    }

    override fun stop() {
        botInstance.awaitShutdown()
    }

}