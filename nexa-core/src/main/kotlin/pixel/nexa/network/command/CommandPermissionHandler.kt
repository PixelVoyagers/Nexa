package pixel.nexa.network.command

import kotlinx.coroutines.runBlocking
import pixel.auxframework.component.annotation.Component
import pixel.auxframework.util.Reference
import pixel.nexa.core.component.NexaEventListener
import pixel.nexa.network.entity.user.UserDataSchema
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.MutableMessageData
import pixel.nexa.network.session.CommandSession

@Component
class CommandPermissionHandler : NexaEventListener, BeforeCommandInteractEventHandler {

    override fun handleBeforeCommandInteractionEvent(
        session: CommandSession,
        command: NexaCommand,
        runCommand: Reference<Boolean>
    ) {
        if (command.getCommandData().getAnnotation().needPermission) {
            val permission = "${command.getCommandData().getIdentifier().getNamespace()}:commands/give-item"
            runCommand.set(
                session.getUser().getDataComponents().getTyped(UserDataSchema.FIELD_PERMISSIONS.second)?.getOrNull()
                    ?.contains(permission) ?: false
            )
            runBlocking {
                session.reply(
                    MutableMessageData().add(
                        MessageFragments.translatable(
                            "text.nexa.command.reply.permission-denied",
                            permission
                        )
                    )
                )
            }
        }
    }

}