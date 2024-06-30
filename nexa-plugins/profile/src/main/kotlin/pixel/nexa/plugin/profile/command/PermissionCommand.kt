package pixel.nexa.plugin.profile.command

import arrow.core.Some
import pixel.nexa.network.command.Command
import pixel.nexa.network.command.CommandSession
import pixel.nexa.network.command.NexaCommand
import pixel.nexa.network.command.OptionTypes
import pixel.nexa.network.entity.user.UserDataSchema
import pixel.nexa.network.entity.user.editDataComponents
import pixel.nexa.plugin.profile.ProfilePlugin

@Command("${ProfilePlugin.PLUGIN_ID}:permission", needPermission = true)
class PermissionCommand : NexaCommand() {

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
        user.editDataComponents {
            val field = get(UserDataSchema.FIELD_PERMISSIONS.second) ?: return@editDataComponents
            val list = field.get().getOrNull()?.toMutableList() ?: mutableListOf()
            if (add) list.add(permission)
            else if (remove) list.remove(permission)
            field.set(Some(list))
        }
        session.reply("√")
    }

}