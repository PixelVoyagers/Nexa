package pixel.nexa.network.entity.guild

import pixel.nexa.core.platform.adapter.NexaBot

interface Guild {

    /**
     * 获取公会ID
     */
    fun getGuildId(): String

    /**
     * 获取公会成员
     */
    fun getMembers(): Set<Member>

    /**
     * 获取公会名称
     */
    fun getGuildName(): String

    /**
     * 获取公会频道
     */
    fun getChannels(): Set<Channel>

    /**
     * 获取机器人实例
     */
    fun getBot(): NexaBot<*>

}

