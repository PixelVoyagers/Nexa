package pixel.nexa.network.session

import pixel.auxframework.component.factory.getComponent
import pixel.nexa.core.platform.adapter.NexaBot
import pixel.nexa.core.resource.AbstractLanguage
import pixel.nexa.core.resource.Languages
import pixel.nexa.network.entity.guild.Channel
import pixel.nexa.network.entity.guild.Guild
import pixel.nexa.network.entity.user.User

/**
 * 会话
 */
interface ISession : ISenderCallback, IReplyCallback {

    /**
     * 获取用户语言
     */
    fun getLanguage(): AbstractLanguage =
        getBot().getAdapter().getContext().getAuxContext().componentFactory().getComponent<Languages>().getDefault()

    /**
     * 获取公会
     */
    fun getGuild(): Guild?

    /**
     * 获取频道
     */
    fun getChannel(): Channel? = getGuild()?.getChannels()?.firstOrNull { it.getChannelId() == getChannelId() }

    /**
     * 获取用户ID
     */
    fun getUserId() = getUser().getUserId()

    /**
     * 获取用户
     */
    fun getUser(): User

    /**
     * 获取频道ID
     */
    fun getChannelId(): String?

    /**
     * 获取公会ID
     */
    fun getGuildId() = getGuild()?.getGuildId()

    /**
     * 获取机器人
     */
    fun getBot(): NexaBot<*>

}

