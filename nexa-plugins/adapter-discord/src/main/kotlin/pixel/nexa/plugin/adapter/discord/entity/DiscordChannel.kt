package pixel.nexa.plugin.adapter.discord.entity

import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import pixel.nexa.core.resource.asset.RootLanguage
import pixel.nexa.network.entity.guild.Channel
import pixel.nexa.plugin.adapter.discord.DiscordBot
import pixel.nexa.plugin.adapter.discord.command.DiscordOriginalMessage
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DiscordChannel(private val bot: DiscordBot, private val channel: net.dv8tion.jda.api.entities.channel.Channel) :
    Channel {

    override fun getBot() = bot
    override fun getGuild() = if (channel is GuildChannel) bot.cachePool.getOrPut(channel.guild.id) {
        DiscordGuild(bot, channel.guild)
    } else null

    override fun getChannelId() = channel.id

    override suspend fun getMessage(messageId: String) = suspendCoroutine { continuation ->
        (channel as MessageChannel).retrieveMessageById(messageId).queue {
            continuation.resume(DiscordOriginalMessage(RootLanguage, it))
        }
    }

}