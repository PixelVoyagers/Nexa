package pixel.nexa.network.command

import pixel.nexa.network.message.MessageData
import pixel.nexa.network.message.MessageSession

abstract class MessageCommandSession : CommandSession() {

    abstract fun getMessageSession(): MessageSession

    override fun getBot() = getMessageSession().getBot()
    override fun getGuild() = getMessageSession().getGuild()
    override fun getUser() = getMessageSession().getUser()
    override fun getChannelId() = getMessageSession().getChannelId()
    override suspend fun reply(message: MessageData) = getMessageSession().reply(message)
    override suspend fun send(message: MessageData) = getMessageSession().send(message)
    override suspend fun replyLazy(message: String, block: suspend () -> MessageData) =
        getMessageSession().replyLazy(message, block)

}