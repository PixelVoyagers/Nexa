package pixel.nexa.plugin.adapter.discord.command

import dev.minn.jda.ktx.messages.MessageCreate
import dev.minn.jda.ktx.messages.MessageEdit
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import pixel.auxframework.component.factory.getComponent
import pixel.nexa.core.resource.AbstractLanguage
import pixel.nexa.core.resource.Languages
import pixel.nexa.network.message.*
import pixel.nexa.network.session.CommandSession
import pixel.nexa.plugin.adapter.discord.DiscordBot
import pixel.nexa.plugin.adapter.discord.DiscordUtils.toNexa
import pixel.nexa.plugin.adapter.discord.entity.DiscordGuild
import pixel.nexa.plugin.adapter.discord.entity.DiscordUser
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DiscordCommandSession(val slash: SlashCommandInteractionEvent, private val bot: DiscordBot) : CommandSession() {

    fun DiscordUser.verify() = this.apply {
        val dataHolder = this.getDataStorage()
        val data = dataHolder.get()
        if (data.locale == null) {
            val languages = bot.getAdapter().getContext().getAuxContext().componentFactory().getComponent<Languages>()
            val languageName = languages.getName(slash.userLocale.toNexa(bot.getAdapter().getContext())) ?: languages.getName(languages.getDefault())
            data.locale = languageName
        }
        dataHolder.set(data)
        refresh()
    }

    override fun getLanguage() = getUser().getLanguageOrNull() ?: slash.userLocale.toNexa(bot.getAdapter().getContext())

    override fun getUser() = getBot().cachePool.getOrPut(slash.user.id) { DiscordUser(getBot(), slash.user).verify() }
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

abstract class DiscordMessage(private val id: String) : Message {

    override fun getMessageId() = id

}

class DiscordOriginalMessage(
    private val language: AbstractLanguage,
    private val message: net.dv8tion.jda.api.entities.Message
) : DiscordMessage(message.id) {
    override suspend fun deleteOriginal() = suspendCoroutine { delete ->
        message.delete().queue { delete.resume(Unit) }
    }

    override suspend fun getOriginal() = suspendCoroutine { get ->
        message.channel.retrieveMessageById(getMessageId()).queue { message -> get.resume(message.toNexaMessage()) }
    }

    override suspend fun editOriginal(block: MutableMessageData.() -> Unit): Message {
        val original = getOriginal()
        return suspendCoroutine { edit ->
            message.editMessage(
                original.toMutable().also(block).toDiscordMessageCreateData(language).toMessageEditData()
            ).queue { queue -> edit.resume(DiscordOriginalMessage(language, queue)) }
        }
    }
}

fun net.dv8tion.jda.api.entities.Message.toNexaMessage(): MutableMessageData {
    val message = MutableMessageData()
    message += MessageFragments.literal(contentRaw)
    message += attachments.map { MessageFragments.file(it.proxy.download().get(), it.fileName) }
    return message
}

fun MessageData.toDiscordMessageCreateData(language: AbstractLanguage): MessageCreateData {
    val list = this.toList()
    return MessageCreate(list.filterIsInstance<TextFragment>().joinToString("", transform = { it.asText(language) })) {
        for (file in list) {
            if (file is FileFragment) {
                files += FileUpload.fromData(file.inputStream(language), file.getFileName(language))
            }
        }
    }
}

fun MessageCreateData.toMessageEditData(): MessageEditData {
    return MessageEdit(content = content, embeds = embeds, files = files, components = components, replace = true)
}