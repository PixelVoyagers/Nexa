package pixel.nexa.network.command

import arrow.core.Option
import pixel.auxframework.util.Reference
import kotlin.reflect.KParameter

interface CommandInteractionEventHandler {

    fun handleCommandInteractionEvent(session: CommandSession, command: NexaCommand, result: Any?): Any?

}

interface BeforeCommandInteractEventHandler {

    fun handleBeforeCommandInteractionEvent(
        session: CommandSession,
        command: NexaCommand,
        runCommand: Reference<Boolean>
    )

}

interface CommandInteractionAutowireEventHandler {

    fun handleCommandInteractionAutowireEvent(
        session: CommandSession,
        parameter: KParameter,
        command: NexaCommand,
        result: Option<Any?>
    ): Option<Any?>

}

interface CommandInteractionArgumentAutowireEventHandler {

    fun handleCommandInteractionArgumentAutowireEvent(
        session: CommandSession,
        parameter: KParameter,
        command: NexaCommand,
        result: Option<Any?>
    ): Option<Any?>

}

interface CommandInteractionOptionAutowireEventHandler {

    fun handleCommandInteractionOptionAutowireEvent(
        session: CommandSession,
        parameter: KParameter,
        optionMapping: OptionMapping,
        command: NexaCommand,
        result: Option<Any?>
    ): Option<Any?>

}

