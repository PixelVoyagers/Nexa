package pixel.nexa.plugin.profile

import pixel.auxframework.component.annotation.Component
import pixel.nexa.network.command.Command
import pixel.nexa.network.command.CommandSession
import pixel.nexa.network.command.NexaCommand
import pixel.nexa.network.command.translatableReply
import pixel.nexa.network.message.MutableMessageData
import pixel.nexa.plugin.profile.handler.SignHandler
import pixel.nexa.plugin.profile.handler.UserExperienceHandler
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

@Component
@Command("${ProfilePlugin.PLUGIN_ID}:sign")
class SignCommand(private val userExperienceHandler: UserExperienceHandler, private val signHandler: SignHandler) :
    NexaCommand() {

    @Action
    suspend fun handle(@Argument session: CommandSession) {
        val user = session.getUser()
        val lastSign = signHandler.getUserLastSign(user)
        val between = lastSign?.let { java.time.Duration.between(lastSign, LocalDateTime.now()) }
        val canSign = lastSign == null || between!!.toDays() >= 1
        session.replyLazy {
            if (canSign) {
                signHandler.setUserLastSign(user, LocalDateTime.now())
                val rewards = signHandler.randomRewards(user)
                rewards.forEach { it.giveTo(user) }
                MutableMessageData().add(
                    translatableReply(
                        "success",
                        rewards.joinToString("\n") {
                            "    " + it.getDisplay().asText(user.getLanguageOrNull() ?: session.getLanguage())
                        })
                )
            } else {
                val duration = Duration.parse("1d") - between!!.abs().toKotlinDuration()
                MutableMessageData().add(
                    translatableReply(
                        "failed",
                        duration.toJavaDuration().truncatedTo(ChronoUnit.SECONDS).toKotlinDuration()
                    )
                )
            }
        }
    }

}