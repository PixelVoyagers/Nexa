package pixel.nexa.plugin.profile.command

import pixel.nexa.network.command.Command
import pixel.nexa.network.command.CommandSession
import pixel.nexa.network.command.NexaCommand
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.MutableMessageData
import pixel.nexa.plugin.profile.ProfilePlugin
import java.time.LocalDate
import kotlin.random.Random
import kotlin.random.nextInt

@Command("${ProfilePlugin.PLUGIN_ID}:luck")
class LuckCommand : NexaCommand() {

    fun random(botId: String, userId: String) = LocalDate.now().let { date ->
        Random("${botId}-${date.year}-${date.dayOfYear}-${userId}".hashCode()).nextInt(-100..100)
    }

    @Action
    suspend fun handle(@Argument session: CommandSession) {
        session.replyLazy {
            val number = random(session.getBot().getSelfId(), session.getUserId())
            MutableMessageData().add(MessageFragments.text("$number%"))
        }
    }

}
