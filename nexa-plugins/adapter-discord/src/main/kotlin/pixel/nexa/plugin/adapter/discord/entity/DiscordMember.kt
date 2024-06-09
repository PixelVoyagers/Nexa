package pixel.nexa.plugin.adapter.discord.entity

import pixel.nexa.network.entity.guild.Member
import pixel.nexa.plugin.adapter.discord.DiscordBot

class DiscordMember(private val bot: DiscordBot, private val member: net.dv8tion.jda.api.entities.Member) : Member {

    override fun getBot() = bot
    override fun getNickname(): String? = member.nickname
    override fun asUser() = bot.cachePool.getOrPut(member.user.id) { DiscordUser(getBot(), member.user) }
    override fun hashCode() = member.hashCode()
    override fun equals(other: Any?) =  other === this || (other != null && other is DiscordMember && other.hashCode() == member.hashCode())

}