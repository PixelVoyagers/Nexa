package pixel.nexa.plugin.adapter.discord.message

import net.dv8tion.jda.api.events.message.GenericMessageEvent
import pixel.nexa.core.resource.AbstractLanguage
import pixel.nexa.network.message.MessageData
import pixel.nexa.network.message.MessageSession
import pixel.nexa.plugin.adapter.discord.DiscordBot
import pixel.nexa.plugin.adapter.discord.command.DiscordOriginalMessage
import pixel.nexa.plugin.adapter.discord.command.toDiscordMessageCreateData
import pixel.nexa.plugin.adapter.discord.entity.DiscordGuild
import pixel.nexa.plugin.adapter.discord.entity.DiscordUser
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DiscordMessageSession(
    private val bot: DiscordBot,
    private val event: GenericMessageEvent,
    private val message: net.dv8tion.jda.api.entities.Message
) : MessageSession() {

    private lateinit var language: AbstractLanguage

    fun setLanguage(language: AbstractLanguage) {
        this.language = language
    }

    private val discordOriginalMessage by lazy {
        DiscordOriginalMessage(language, message)
    }

    override fun getMessage() = discordOriginalMessage

    override fun getBot() = bot

    override suspend fun send(message: MessageData) = suspendCoroutine { continuation ->
        event.channel.sendMessage(message.toDiscordMessageCreateData(getLanguage())).queue {
            continuation.resume(DiscordOriginalMessage(getLanguage(), it))
        }
    }

    override fun getGuild() = DiscordGuild(getBot(), event.guild)

    override fun getUser() =
        getBot().cachePool.getOrPut(message.author.id) { DiscordUser(getBot(), message.author) }

    override suspend fun reply(message: MessageData) = send(message)

    override suspend fun replyLazy(message: String, block: suspend () -> MessageData) = send(block())

    override fun getChannelId() = event.channel.id

    override fun getLanguage() = getUser().getLanguageOrNull() ?: language

}