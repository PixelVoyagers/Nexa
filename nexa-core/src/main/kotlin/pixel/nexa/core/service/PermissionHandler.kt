package pixel.nexa.core.service

import arrow.core.Some
import kotlinx.coroutines.runBlocking
import pixel.auxframework.component.annotation.Service
import pixel.auxframework.core.registry.Identifier
import pixel.auxframework.core.registry.identifierOf
import pixel.auxframework.util.useAuxConfig
import pixel.nexa.core.NexaCore
import pixel.nexa.core.component.AfterNexaContextStarted
import pixel.nexa.core.platform.NexaContext
import pixel.nexa.network.entity.user.User
import pixel.nexa.network.entity.user.UserDataSchema
import pixel.nexa.network.entity.user.editDataComponents

@Service
class PermissionHandler(nexaCore: NexaCore) : AfterNexaContextStarted {

    companion object {
        const val PERMISSION_ADMIN = "${NexaCore.DEFAULT_NAMESPACE}:admin"
    }

    class Config{

        class Admin {
            lateinit var bot: String
            lateinit var platform: String
            lateinit var user: String
        }

        val admin: MutableSet<Admin> = mutableSetOf()

    }

    val config = nexaCore.getDirectory("config/nexa/core/").useAuxConfig<Config>("permission.yml")

    override fun afterNexaContextStarted(context: NexaContext) = runBlocking {
        val adapters = context.getAdapters().flatMap { it.getBots() }.associateBy {
            "${it.getAdapter().getPlatform()};${it.getName()}"
        }
        val admin = config.admin.associateBy {
            "${it.platform.trim()};${it.bot.trim()}"
        }.mapNotNull { adapters[it.key]?.internal()?.getUserById(it.value.user) }
        for (user in admin) {
            addPermissions(user, identifierOf(PERMISSION_ADMIN))
        }
    }


    fun getPermissions(user: User) = user.getDataComponents().getTyped(UserDataSchema.FIELD_PERMISSIONS.second)?.getOrNull()?.toSet() ?: emptySet()

    fun addPermissions(user: User, vararg permission: Identifier) {
        val permissions = getPermissions(user).toMutableSet()
        permissions.addAll(permission)
        setPermissions(user, permissions)
    }

    fun removePermissions(user: User, vararg permission: Identifier) {
        val permissions = getPermissions(user).toMutableSet()
        permissions.removeAll(permission.toSet())
        setPermissions(user, permissions)
    }

    fun setPermissions(user: User, permissions: Set<Identifier>) {
        user.editDataComponents {
            get(UserDataSchema.FIELD_PERMISSIONS.second)?.set(Some(permissions.toList()))
        }
    }

    fun isAdmin(user: User) = getPermissions(user).contains(identifierOf(PERMISSION_ADMIN))

    fun hasPermission(user: User, permission: Identifier) = getPermissions(user).contains(permission)

}