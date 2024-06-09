package pixel.nexa.plugin.adapter.discord

import com.neovisionaries.ws.client.WebSocketFactory
import dev.minn.jda.ktx.interactions.commands.updateCommands
import dev.minn.jda.ktx.jdabuilder.light
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction
import okhttp3.OkHttpClient
import pixel.auxframework.component.factory.getComponent
import pixel.nexa.core.platform.adapter.AbstractNexaBot
import pixel.nexa.core.util.ConstantUtils
import pixel.nexa.network.command.CommandContainer
import pixel.nexa.network.command.NexaCommand
import pixel.nexa.plugin.adapter.discord.DiscordUtils.putDiscordTranslations
import pixel.nexa.plugin.adapter.discord.command.DiscordCommandSession
import pixel.nexa.plugin.adapter.discord.entity.DiscordUser
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DiscordBotListener(private val bot: DiscordBot) : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val commands =
            bot.getAdapter().getContext().getAuxContext().componentFactory().getComponent<CommandContainer>().getAll()
        val command = commands.first {
            event.fullCommandName == it.getCommandData().getIdentifier()
                .format { namespace, path -> "$namespace-${path.split("/").joinToString("-")}" }
        }
        val session = DiscordCommandSession(event, bot)
        runBlocking {
            command.getCommandData().getAction().invoke(session)
        }
    }

}

class DiscordBotInternal(private val bot: DiscordBot) : AbstractNexaBot.Internal(bot) {

    override suspend fun getUserById(id: String) = suspendCoroutine { continuation ->
        bot.getInstance().retrieveUserById(id).queue {
            continuation.resume(bot.cachePool.getOrPut(it.id) { DiscordUser(bot, it) })
        }
    }

}

class DiscordBot(private val discordAdapter: DiscordAdapter, private val config: Map<String, Any>) :
    AbstractNexaBot<DiscordBot>() {

    override fun internal() = DiscordBotInternal(this)

    val cachePool = DiscordCachePool()

    override fun getAdapter() = discordAdapter

    private lateinit var botInstance: JDA
    fun getInstance() = botInstance

    fun login() = light(config["token"].toString(), true) {
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
        val commands = applicationContext.componentFactory().getComponent<CommandContainer>()
        val discordCommands = mutableSetOf<CommandData>()
        for (command in commands.getAll()) {
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