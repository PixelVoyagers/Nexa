package pixel.nexa.plugin.adapter.discord.entity

import pixel.nexa.network.entity.guild.Guild
import pixel.nexa.plugin.adapter.discord.DiscordBot


class DiscordGuild(private val bot: DiscordBot, private val guild: net.dv8tion.jda.api.entities.Guild) : Guild {

    override fun getBot() = bot

    override fun getGuildId() = guild.id
    override fun getMembers() = guild.loadMembers().get().map {
        bot.cachePool.getOrPut("${it.guild.id}:${it.user.id}") {
            DiscordMember(bot, it)
        }
    }.toSet()

    override fun getChannels() = guild.channels.map {
        bot.cachePool.getOrPut("${it.guild.id}:${it.id}") {
            DiscordChannel(bot, it)
        }
    }.toSet()

    override fun getGuildName() = guild.name
    override fun hashCode() = guild.hashCode()
    override fun equals(other: Any?) =
        other === this || (other != null && other is DiscordGuild && other.guild.hashCode() == guild.hashCode())
}