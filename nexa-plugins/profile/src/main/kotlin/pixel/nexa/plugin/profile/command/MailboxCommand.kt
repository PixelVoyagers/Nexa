package pixel.nexa.plugin.profile.command

import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.resource.AssetsMap
import pixel.nexa.network.command.*
import pixel.nexa.network.entity.user.User
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.MutableMessageData
import pixel.nexa.plugin.profile.ProfilePlugin
import pixel.nexa.plugin.profile.handler.Mail
import pixel.nexa.plugin.profile.handler.MailHandler
import kotlin.math.max
import kotlin.math.min

@Command("${ProfilePlugin.PLUGIN_ID}:mailbox")
class MailboxCommand(private val assetsMap: AssetsMap, private val mailHandler: MailHandler) : NexaCommand() {

    fun getChunks(user: User) = mailHandler.getUserMailbox(user)
        .sortedBy { it.isReceived }
        .chunked(4)

    @Action
    suspend fun handle(
        @Argument session: CommandSession,
        @Option(required = false, autoComplete = Option.AutoCompleteMode.ENABLED) page: Int = 1,
        @Option(required = false, autoComplete = Option.AutoCompleteMode.ENABLED, name = "mail") viewMail: Long? = null,
        @Option(required = false, type = OptionTypes.BOOLEAN) receive: Boolean = false,
        @Option(required = false, type = OptionTypes.BOOLEAN) remove: Boolean = false,
    ) {
        session.replyLazy {
            if (receive && viewMail != null) handleReceive(session, viewMail)
            else if (remove && viewMail != null) handleRemove(session, viewMail)
            else if (viewMail != null) handleViewEmail(session, viewMail)
            else handleList(session, page)
        }
    }

    @AutoComplete("page")
    fun pageAutoComplete(autoComplete: CommandAutoComplete) {
        val range = 0..<getChunks(autoComplete.user).size
        autoComplete.result += range.map { CommandAutoComplete.Choice((it + 1).toString()) }
    }

    @AutoComplete("mail")
    fun emailViewAutoComplete(autoComplete: CommandAutoComplete) {
        autoComplete.result += mailHandler.getUserMailbox(autoComplete.user).mapNotNull { it.id }.map {
            CommandAutoComplete.Choice(it.toString())
        }
    }

    fun handleReceive(session: CommandSession, mailId: Long): MutableMessageData {
        val mailbox = mailHandler.getUserMailbox(session.getUser())
        val mail = mailbox.first { it.id == mailId }
        mail.attachments.removeIf { it.giveTo(session.getUser()) }
        if (mail.attachments.isEmpty()) mail.isReceived = true
        mailHandler.setUserMailbox(session.getUser(), mailbox)
        return MutableMessageData().add(MessageFragments.text("√"))
    }

    fun handleRemove(session: CommandSession, mailId: Long): MutableMessageData {
        val mailbox = mailHandler.getUserMailbox(session.getUser()).toMutableList()
        val mail = mailbox.first { it.id == mailId }
        if (mail.attachments.isEmpty() && mail.isReceived) {
            mailbox.remove(mail)
            mailHandler.setUserMailbox(session.getUser(), mailbox)
            return MutableMessageData().add(MessageFragments.text("√"))
        } else {
            return MutableMessageData().add(MessageFragments.text("×"))
        }
    }

    fun handleViewEmail(session: CommandSession, mailId: Long): MutableMessageData {
        val mail = mailHandler.getUserMailbox(session.getUser()).first { it.id == mailId }
        return MutableMessageData().add(
            MessageFragments.pageView(
                assetsMap.getPage(identifierOf("${ProfilePlugin.PLUGIN_ID}:mailbox/mail.html"))
            ) {
                val language = session.getUser().getLanguageOrNull() ?: session.getLanguage()
                put("language", language)
                put("mail", mail)
            }
        )
    }

    fun handleList(
        session: CommandSession,
        page: Int
    ): MutableMessageData {
        val mails = getChunks(session.getUser())
        val pageIndex = max(min(max(0, page - 1), page - 1), 0)
        return MutableMessageData().add(
            MessageFragments.pageView(
                assetsMap.getPage(identifierOf("${ProfilePlugin.PLUGIN_ID}:mailbox/index.html"))
            ) {
                val language = session.getUser().getLanguageOrNull() ?: session.getLanguage()
                put("page", "${pageIndex + 1} / ${max(mails.size, 1)}")
                put("language", language)
                put("mails", mails.getOrNull(pageIndex) ?: emptyList<Mail>())
            }
        )
    }

}