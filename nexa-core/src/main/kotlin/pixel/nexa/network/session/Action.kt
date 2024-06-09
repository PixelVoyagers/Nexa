package pixel.nexa.network.session

import pixel.nexa.network.message.Message
import pixel.nexa.network.message.MessageData
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.MutableMessageData


/**
 * 消息发送器
 */
interface ISenderCallback {

    /**
     * 发送消息
     */
    suspend fun send(message: String) = send(MutableMessageData().add(MessageFragments.literal(message)))

    /**
     * 发送消息
     */
    suspend fun send(message: MessageData): Message

}

interface IReplyCallback {

    /**
     * 回复消息
     */
    suspend fun reply(message: String) = reply(MutableMessageData().add(MessageFragments.literal(message)))

    /**
     * 回复消息
     */
    suspend fun reply(message: MessageData): Message

    /**
     * 回复消息
     */
    suspend fun replyLazy(message: String = "...", block: suspend () -> MessageData): Message
}
