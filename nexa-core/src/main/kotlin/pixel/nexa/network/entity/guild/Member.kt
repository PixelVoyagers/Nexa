package pixel.nexa.network.entity.guild

import pixel.nexa.core.platform.adapter.NexaBot
import pixel.nexa.network.entity.user.User

interface Member {

    /**
     * 获取机器人实例
     */
    fun getBot(): NexaBot<*>

    /**
     * 获取用户ID
     */
    fun getUserId(): String = asUser().getUserId()

    /**
     * 获取昵称
     */
    fun getNickname(): String?

    /**
     * 转为用户
     */
    fun asUser(): User

}
