package pixel.nexa.network.entity.user

import pixel.nexa.core.platform.adapter.NexaBot
import java.io.InputStream

abstract class User(private val bot: NexaBot<*>) {

    /**
     * 获取机器人实例
     */
    fun getBot() = bot

    /**
     * 获取用户ID
     */
    abstract fun getUserId(): String

    /**
     * 获取标准用户名称
     */
    abstract fun getUserName(): String

    /**
     * 获取用户名称
     */
    abstract fun getEffectiveName(): String

    /**
     * 获取标准头像字节流
     */
    abstract fun getAvatarStream(): InputStream?

    /**
     * 获取头像字节流
     */
    abstract fun getEffectiveAvatarStream(): InputStream?

    /**
     * 获取默认头像字节流
     */
    abstract fun getDefaultAvatarStream(): InputStream?

    /**
     * 获取标准头像URL
     */
    abstract fun getAvatarURL(): String?

    /**
     * 获取头像字节流
     */
    abstract fun getEffectiveAvatarURL(): String?

    /**
     * 获取默认头像字节流
     */
    abstract fun getDefaultAvatarURL(): String?

}