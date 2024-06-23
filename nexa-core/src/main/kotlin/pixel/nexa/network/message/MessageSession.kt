package pixel.nexa.network.message

import pixel.nexa.network.session.ISession

/**
 * 消息会话
 */
abstract class MessageSession : ISession {

    abstract fun getMessage(): Message

}

