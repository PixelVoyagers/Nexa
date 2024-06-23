package pixel.nexa.network.command

import pixel.nexa.network.session.ISession

/**
 * 指令会话
 */
abstract class CommandSession : ISession {

    abstract fun getCommand(): NexaCommand

    abstract fun options(): Set<OptionMapping>

}
