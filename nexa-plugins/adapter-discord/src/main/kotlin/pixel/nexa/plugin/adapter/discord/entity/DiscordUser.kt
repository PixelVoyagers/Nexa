package pixel.nexa.plugin.adapter.discord.entity

import pixel.nexa.network.entity.user.User
import pixel.nexa.plugin.adapter.discord.DiscordBot
import java.io.InputStream

class DiscordUser(bot: DiscordBot, private val user: net.dv8tion.jda.api.entities.User) : User(bot) {

    override fun getUserName() = user.name
    override fun getEffectiveName() = user.effectiveName

    override fun getUserId() = user.id
    override fun getAvatarStream() = user.avatar?.download()?.get()
    override fun getAvatarURL() = user.avatarUrl
    override fun getEffectiveAvatarStream(): InputStream = user.effectiveAvatar.download().get()
    override fun getEffectiveAvatarURL() = user.effectiveAvatarUrl
    override fun getDefaultAvatarStream(): InputStream = user.defaultAvatar.download().get()
    override fun getDefaultAvatarURL() = user.defaultAvatarUrl

}