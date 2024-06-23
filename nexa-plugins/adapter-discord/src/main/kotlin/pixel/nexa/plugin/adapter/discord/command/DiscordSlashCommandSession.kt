package pixel.nexa.plugin.adapter.discord.command

import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import pixel.nexa.network.command.CommandSession
import pixel.nexa.network.command.NexaCommand
import pixel.nexa.network.message.Message
import pixel.nexa.network.message.MessageData
import pixel.nexa.network.message.MutableMessageData
import pixel.nexa.network.message.toMutable
import pixel.nexa.plugin.adapter.discord.DiscordBot
import pixel.nexa.plugin.adapter.discord.DiscordUtils.toNexa
import pixel.nexa.plugin.adapter.discord.entity.DiscordGuild
import pixel.nexa.plugin.adapter.discord.entity.DiscordUser
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DiscordSlashCommandSession(
    private val command: NexaCommand,
    val slash: SlashCommandInteractionEvent,
    private val bot: DiscordBot
) : CommandSession() {

    private val options = slash.options.map { optionMapping ->
        DiscordOptionMapping(
            command.getCommandData().getOptions().firstOrNull { it.getName() == optionMapping.name },
            optionMapping
        )
    }.toSet()

    override fun options() = options

    override fun getCommand() = command

    override fun getLanguage() = getUser().getLanguageOrNull() ?: slash.userLocale.toNexa(bot.getAdapter().getContext())

    override fun getUser() =
        getBot().cachePool.getOrPut(slash.user.id) { slash.verify(DiscordUser(getBot(), slash.user)) }

    override fun getChannelId() = slash.channelId
    override fun getGuild() = slash.guild?.let { DiscordGuild(getBot(), it) }
    override fun getBot() = bot

    override suspend fun send(message: MessageData): Message = suspendCoroutine { continuation ->
        slash.channel.sendMessage(message.toDiscordMessageCreateData(getLanguage())).queue {
            continuation.resume(DiscordOriginalMessage(getLanguage(), it))
        }
    }

    override suspend fun reply(message: MessageData): Message = suspendCoroutine { continuation ->
        slash.reply(message.toDiscordMessageCreateData(getLanguage())).queue {
            continuation.resume(
                object : DiscordMessage(it.id) {
                    override suspend fun deleteOriginal() = suspendCoroutine { delete ->
                        it.deleteOriginal().queue { delete.resume(Unit) }
                    }

                    override suspend fun getOriginal() = suspendCoroutine { get ->
                        it.retrieveOriginal().queue { message -> get.resume(message.toNexaMessage()) }
                    }

                    override suspend fun editOriginal(block: MutableMessageData.() -> Unit): Message {
                        val original = getOriginal()
                        return suspendCoroutine { edit ->
                            it.editOriginal(
                                original.toMutable().also(block).toDiscordMessageCreateData(getLanguage())
                                    .toMessageEditData()
                            ).queue { queue -> edit.resume(DiscordOriginalMessage(getLanguage(), queue)) }
                        }
                    }
                }
            )
        }
    }


    override suspend fun replyLazy(message: String, block: suspend () -> MessageData): Message =
        suspendCoroutine { continuation ->
            slash.reply(message).queue {
                it.editOriginal(runBlocking { block() }.toDiscordMessageCreateData(getLanguage()).toMessageEditData())
                    .queue { message ->
                        continuation.resume(DiscordOriginalMessage(getLanguage(), message))
                    }
            }
        }

}