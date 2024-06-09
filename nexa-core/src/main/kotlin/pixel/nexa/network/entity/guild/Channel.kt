package pixel.nexa.network.entity.guild

import pixel.nexa.core.platform.adapter.NexaBot
import pixel.nexa.network.message.Message

interface Channel {

    /**
     * 获取机器人实例
     */
    fun getBot(): NexaBot<*>

    /**
     * 获取公会
     */
    fun getGuild(): Guild?

    /**
     * 获取频道ID
     */
    fun getChannelId(): String

    /**
     * 获取频道消息
     */
    suspend fun getMessage(messageId: String): Message

}