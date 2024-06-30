package pixel.nexa.plugin.profile.command

import pixel.aurora.compiler.AuroraCompiler
import pixel.aurora.compiler.parser.Parser
import pixel.aurora.compiler.tokenizer.TokenBuffer
import pixel.aurora.compiler.tokenizer.Tokenizer
import pixel.nexa.core.data.tag.CompoundTag
import pixel.nexa.core.data.tag.ListTagParser
import pixel.nexa.core.platform.NexaContext
import pixel.nexa.network.command.Command
import pixel.nexa.network.command.CommandSession
import pixel.nexa.network.command.NexaCommand
import pixel.nexa.network.command.OptionTypes
import pixel.nexa.network.entity.user.User
import pixel.nexa.network.entity.user.UserSelector
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.MutableMessageData
import pixel.nexa.plugin.profile.ProfilePlugin
import pixel.nexa.plugin.profile.handler.Mail
import pixel.nexa.plugin.profile.handler.MailHandler
import java.nio.CharBuffer

@Command("${ProfilePlugin.PLUGIN_ID}:mail-send", needPermission = true)
class MailSendCommand(private val nexaContext: NexaContext, private val mailHandler: MailHandler) : NexaCommand() {

    @Action
    suspend fun handle(
        @Argument session: CommandSession,
        @Option(name = "title", type = OptionTypes.STRING, required = true) inputTitle: String,
        @Option(name = "content", type = OptionTypes.STRING, required = true) inputContent: String,
        @Option(name = "user-selector", type = OptionTypes.STRING, required = false) inputUserSelector: String? = null,
        @Option(name = "user", type = OptionTypes.USER, required = false) inputUser: String? = null,
        @Option(name = "attachments", type = OptionTypes.STRING, required = false) inputAttachments: String? = null
    ) {
        session.replyLazy {
            val users = mutableListOf<User>()
            if (inputUserSelector != null) users += with(nexaContext) {
                UserSelector.parse(inputUserSelector.trim()).matchUsers()
            }
            if (inputUser != null) users += session.getBot().internal().getUserById(inputUser)
            if (inputUserSelector == null && inputUser == null) users += session.getUser()
            val title = MessageFragments.literal(inputTitle)
            val content = MessageFragments.literal(inputContent)
            val attachments = inputAttachments?.let {
                ListTagParser()
                    .setState(Parser.State(AuroraCompiler.BLANK_URI, TokenBuffer(Tokenizer(CharBuffer.wrap(it)))))
                    .parse()
            }?.filterIsInstance<CompoundTag>()?.map(mailHandler.mailDataType::deserializeAttachment)?.toMutableList()
                ?: mutableListOf()
            for (user in users) {
                mailHandler.addUserMail(user, Mail(title, content, attachments))
            }
            MutableMessageData().add(
                MessageFragments.text("√")
            )
        }
     }

}