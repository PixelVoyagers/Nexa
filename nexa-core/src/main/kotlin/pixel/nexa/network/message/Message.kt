package pixel.nexa.network.message

/**
 * 消息
 */
interface Message {

    /**
     * 获取消息ID
     */
    fun getMessageId(): String

    /**
     * 修改原消息
     */
    suspend fun editOriginal(block: MutableMessageData.() -> Unit): Message

    /**
     * 消除原消息
     */
    suspend fun deleteOriginal()

    /**
     * 获取原消息
     */
    suspend fun getOriginal(): MessageData

}