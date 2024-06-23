package pixel.nexa.network.command

import kotlinx.coroutines.runBlocking
import pixel.auxframework.component.annotation.Component
import pixel.auxframework.component.factory.ComponentFactory
import pixel.auxframework.component.factory.getComponents
import pixel.auxframework.core.registry.Identifier
import pixel.nexa.core.component.NexaEventListener
import pixel.nexa.network.message.GenericMessageEventHandler
import pixel.nexa.network.message.MessageSession

interface MessageCommandDriver {

    suspend fun createSession(session: MessageSession): CommandSession?

    fun getMessageCommandHandlerIdentifier(): Identifier

}

@Component
class MessageCommandService(
    private val commandService: CommandService,
    private val componentFactory: ComponentFactory
) : NexaEventListener, GenericMessageEventHandler {

    fun isEnabled() = commandService.config.messageCommandConfig.enabled

    fun getPrefix() = commandService.config.messageCommandConfig.prefix

    fun getDriver() = componentFactory.getComponents<MessageCommandDriver>()
        .first { it.getMessageCommandHandlerIdentifier() == commandService.config.messageCommandConfig.driver }

    override fun handleGenericMessageEvent(messageSession: MessageSession) {
        if (!isEnabled()) return
        runBlocking {
            val session = getDriver().createSession(messageSession) ?: return@runBlocking
            session.getCommand().getCommandData().getAction().invoke(session)
        }
    }

}
