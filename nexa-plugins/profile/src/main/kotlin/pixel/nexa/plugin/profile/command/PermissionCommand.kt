package pixel.nexa.plugin.profile.command

import pixel.auxframework.component.annotation.Component
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.NexaCore
import pixel.nexa.core.service.PermissionHandler
import pixel.nexa.network.command.Command
import pixel.nexa.network.command.CommandSession
import pixel.nexa.network.command.NexaCommand
import pixel.nexa.network.command.OptionTypes
import pixel.nexa.plugin.profile.ProfilePlugin

@Command("${ProfilePlugin.PLUGIN_ID}:permission", needPermission = true)
@Component
class PermissionCommand(private val permissionHandler: PermissionHandler) : NexaCommand() {

    @Action
    suspend fun handle(
        @Argument session: CommandSession,
        @Option(name = "user", type = OptionTypes.USER, required = true) inputUserId: String,
        @Option(type = OptionTypes.STRING, required = true) permission: String,
        @Option(type = OptionTypes.BOOLEAN, required = false) add: Boolean = false,
        @Option(type = OptionTypes.BOOLEAN, required = false) remove: Boolean = false
    ) {
        val split = inputUserId.split(":")
        if (split.isEmpty()) return
        val user = if (split.size == 1) session.getBot().internal().getUserById(split.first())
        else session.getBot().getAdapter().getContext().getAdapters().first { it.getPlatform() == split.first() }
            .getBots().firstNotNullOf {
                kotlin.runCatching {
                    it.internal().getUserById(split.subList(1, split.size).joinToString(separator = ":"))
                }.getOrNull()
            }
        if (add) permissionHandler.addPermissions(user, identifierOf(permission, NexaCore.DEFAULT_NAMESPACE))
        if (remove) permissionHandler.removePermissions(user, identifierOf(permission, NexaCore.DEFAULT_NAMESPACE))
        session.reply("√")
    }

}