package pixel.nexa.plugin.adapter.discord.command

import arrow.core.None
import arrow.core.Option
import pixel.auxframework.component.annotation.Component
import pixel.auxframework.component.factory.getComponents
import pixel.nexa.core.platform.NexaContext
import pixel.nexa.network.command.CommandInteractionAutowireEventHandler
import pixel.nexa.network.command.CommandInteractionOptionAutowireEventHandler
import pixel.nexa.network.command.NexaCommand
import pixel.nexa.network.session.CommandSession
import kotlin.reflect.KParameter
import kotlin.reflect.full.hasAnnotation

@Component
class DiscordCommandAutowireProcessor(private val context: NexaContext) : CommandInteractionAutowireEventHandler {

    override fun handleCommandInteractionAutowireEvent(
        session: CommandSession,
        parameter: KParameter,
        command: NexaCommand,
        result: Option<Any?>
    ): Option<Any?> {
        if (session !is DiscordCommandSession) return result
        if (!parameter.hasAnnotation<NexaCommand.Option>()) return result
        val optionMapping = session.slash.getOption(NexaCommand.Option.getName(parameter))
        if (optionMapping != null) {
            val mapping = DiscordOptionMapping(command.getCommandData().getOptions().firstOrNull { it.getName() == optionMapping.name }, optionMapping)
            var handlerResult: Option<Any?> = None
            for (handler in command.getNexaContext().getAuxContext().componentFactory().getComponents<CommandInteractionOptionAutowireEventHandler>()) {
                handlerResult = handler.handleCommandInteractionOptionAutowireEvent(session, parameter, mapping, command, handlerResult)
            }
            return handlerResult
        }
        return result
    }


}