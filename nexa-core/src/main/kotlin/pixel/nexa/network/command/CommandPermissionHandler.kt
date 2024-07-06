package pixel.nexa.network.command

import kotlinx.coroutines.runBlocking
import pixel.auxframework.component.annotation.Component
import pixel.auxframework.core.registry.identifierOf
import pixel.auxframework.util.Reference
import pixel.nexa.core.NexaCore
import pixel.nexa.core.component.NexaEventListener
import pixel.nexa.core.service.PermissionHandler
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.MutableMessageData

@Component
class CommandPermissionHandler(private val permissionHandler: PermissionHandler) : NexaEventListener, BeforeCommandInteractEventHandler {

    override fun handleBeforeCommandInteractionEvent(
        session: CommandSession,
        command: NexaCommand,
        runCommand: Reference<Boolean>
    ) {
        if (command.getCommandData().getAnnotation().needPermission) {
            val permission = "${command.getCommandData().getIdentifier().getNamespace()}:commands/${
                command.getCommandData().getIdentifier().getPath()
            }"
            runCommand.set(
                permissionHandler.hasPermission(session.getUser(), identifierOf(permission, NexaCore.DEFAULT_NAMESPACE)) || permissionHandler.isAdmin(session.getUser())
            )
            if (!runCommand.get()) runBlocking {
                session.reply(
                    MutableMessageData().add(
                        MessageFragments.translatable(
                            "text.nexa.command.reply.permission.denied",
                            permission
                        )
                    )
                )
            }
        }
    }

}