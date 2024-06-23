package pixel.nexa.network.command

import pixel.auxframework.component.annotation.Service
import pixel.auxframework.component.factory.ComponentDefinition
import pixel.auxframework.component.factory.ComponentPostProcessor
import pixel.auxframework.core.registry.Identifier
import pixel.auxframework.core.registry.ResourceKey
import pixel.auxframework.core.registry.identifierOf
import pixel.auxframework.util.useAuxConfig
import pixel.nexa.core.NexaCore
import pixel.nexa.core.component.AfterNexaContextStarted
import pixel.nexa.core.platform.NexaContext
import pixel.nexa.core.registry.createRegistry
import kotlin.reflect.full.hasAnnotation

@Service
class CommandService(nexaCore: NexaCore, context: NexaContext) : ComponentPostProcessor, AfterNexaContextStarted {

    val commands = context.createRegistry<NexaCommand>(
        ResourceKey(
            identifierOf("root", NexaCore.DEFAULT_NAMESPACE),
            identifierOf("command", NexaCore.DEFAULT_NAMESPACE)
        )
    ).apply {
        unfreeze()
    }

    fun getCommands() = commands.toSet().map(Pair<Identifier, NexaCommand>::second)

    override fun afterNexaContextStarted(context: NexaContext) {
        commands.freeze()
    }

    data class Config(
        val commands: MutableMap<Identifier, CommandConfig> = mutableMapOf(),
        val messageCommandConfig: MessageCommandConfig = MessageCommandConfig()
    ) {

        data class MessageCommandConfig(
            var enabled: Boolean = false,
            var prefix: String = "/",
            var driver: Identifier? = null
        )

        data class CommandConfig(var enabled: Boolean = true)

    }

    val config = nexaCore.getDirectory("config/nexa/command/").useAuxConfig<Config>("command.yml")

    override fun processComponent(componentDefinition: ComponentDefinition, instance: Any?) = instance.also {
        if (instance !is NexaCommand) return@also
        if (config.commands[instance.getCommandData().getIdentifier()]?.enabled == false) return@also
        if (instance::class.hasAnnotation<Command>()) {
            commands.register(instance.getCommandData().getIdentifier()) { instance }
        }
    }

}