package pixel.nexa.network.command

import arrow.core.None
import arrow.core.Some
import pixel.auxframework.component.annotation.Autowired
import pixel.auxframework.component.annotation.Component
import pixel.auxframework.component.annotation.Repository
import pixel.auxframework.component.annotation.Service
import pixel.auxframework.component.factory.*
import pixel.auxframework.context.builtin.SimpleListRepository
import pixel.auxframework.core.registry.Identifier
import pixel.auxframework.core.registry.identifierOf
import pixel.auxframework.util.useAuxConfig
import pixel.nexa.core.NexaCore
import pixel.nexa.core.platform.NexaContext
import pixel.nexa.core.util.StringUtils
import pixel.nexa.network.session.CommandSession
import pixel.nexa.network.session.ISession
import java.lang.reflect.Method
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.kotlinFunction


/**
 * 指令类
 */
@Suppress("LeakingThis")
abstract class NexaCommand {

    private val commandData = CommandData(this)
    open val commandTranslator: CommandTranslator = CommandTranslator(getCommandData().getIdentifier())

    @Autowired
    var context: NexaContext? = null

    /**
     * 获取指令内部数据
     */
    fun getCommandData() = commandData

    /**
     * 获取指令所在上下文
     */
    fun getNexaContext() = context!!

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class AutoComplete(vararg val option: String)

    @Target(AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Option(
        val name: String = "",
        val type: String = OptionTypes.STRING,
        val autoComplete: AutoCompleteMode = AutoCompleteMode.DISABLED,
        val required: Boolean = true,
        val register: Boolean = true
    ) {
        enum class AutoCompleteMode {
            DEFAULT, ENABLED, DISABLED
        }

        companion object {

            fun getName(parameter: KParameter): String {
                val annotation = parameter.findAnnotation<Option>()
                return if (annotation == null) StringUtils.lowerCamelToLowerKebab(parameter.name.toString())
                else if (annotation.name.isEmpty()) StringUtils.lowerCamelToLowerKebab(parameter.name.toString())
                else annotation.name
            }

        }
    }

    @Target(AnnotationTarget.VALUE_PARAMETER)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Argument

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Action

}

class CommandData(private val command: NexaCommand) {

    /**
     * 获取指令
     */
    fun getNexaCommand() = command

    private val action = CommandAction(this)

    /**
     * 获取指令触发器
     */
    fun getAction() = action

    private val options: Set<Option> =
        getAction().getMethod().parameters.asSequence().filter { it.hasAnnotation<NexaCommand.Option>() }
            .map {
                val annotation = it.findAnnotation<NexaCommand.Option>()!!
                Pair(
                    Option(
                        this.getNexaCommand(),
                        NexaCommand.Option.getName(it),
                        identifierOf(annotation.type, NexaCore.DEFAULT_NAMESPACE),
                        annotation.autoComplete,
                        annotation.required,
                        receiverType = it.type.classifier as? KClass<*>
                    ), annotation.register
                )
            }.filter { it.second }.map { it.first }.toSet()

    fun getOptions() = options

    private val autoComplete: MutableMap<Array<out String>, KFunction<*>> =
        getNexaCommand()::class.memberFunctions.filter {
            it.hasAnnotation<NexaCommand.AutoComplete>()
        }.associateBy { it.findAnnotation<NexaCommand.AutoComplete>()!!.option }.toMutableMap()

    fun getAutoComplete() = autoComplete

    /**
     * 获取指令注解
     * @see Command
     */
    fun getAnnotation(): Command = command::class.java.getAnnotation(Command::class.java)

    /**
     * 获取指令标识
     */
    fun getIdentifier() = identifierOf(getAnnotation().name)

}

class CommandAction(private val data: CommandData) {

    val method: Method = data.getNexaCommand().javaClass.methods.filter {
        it.isAnnotationPresent(NexaCommand.Action::class.java)
    }.let {
        if (it.size == 1) return@let it.first()
        else if (it.isEmpty()) throw IllegalStateException("Empty command action")
        else throw IllegalStateException("Duplicate command actions")
    }

    fun getMethod() = method.kotlinFunction!!
    fun getCommandData() = data

    /**
     * 执行动作
     */
    suspend fun invoke(session: CommandSession): Any? {
        val autowired = mutableMapOf<KParameter, Any?>()
        val command = getCommandData().getNexaCommand()
        val factory = command.getNexaContext().getAuxContext().componentFactory()
        for (parameter in getMethod().parameters) {
            if (parameter.name == null) {
                autowired[parameter] = command
                continue
            }
            var result: arrow.core.Option<Any?> = None
            for (handler in factory.getComponents<CommandInteractionAutowireEventHandler>()) {
                val handlerResult = handler.handleCommandInteractionAutowireEvent(session, parameter, command, result)
                if (handlerResult.isSome()) result = handlerResult
            }
            if (!(parameter.isOptional && result.isNone())) autowired[parameter] = result.getOrNull()
        }
        var result = getMethod().callSuspendBy(autowired)
        for (handler in factory.getComponents<CommandInteractionEventHandler>()) {
            result = handler.handleCommandInteractionEvent(session, command, result)
        }
        return result
    }

}

data class CommandAutoComplete(val input: String, val option: String, val result: MutableList<Choice>) {

    data class Choice(val display: String, val value: String = display, val important: Boolean = false)

}

@Repository
interface CommandContainer : SimpleListRepository<NexaCommand>

@Service
class CommandService(private val container: CommandContainer, nexaCore: NexaCore) : ComponentPostProcessor {

    data class Config(val commands: MutableMap<Identifier, CommandConfig> = mutableMapOf()) {

        data class CommandConfig(var enabled: Boolean = true)

    }

    val config = nexaCore.getDirectory("config/nexa/command/").useAuxConfig<Config>("command.yml")

    override fun processComponent(componentDefinition: ComponentDefinition, instance: Any?) = instance.also {
        if (instance !is NexaCommand) return@also
        if (config.commands[instance.getCommandData().getIdentifier()]?.enabled == false) return@also
        if (instance::class.hasAnnotation<Command>())
            container.add(instance)
    }

}

@Component
class CommandAutowireProcessor : CommandInteractionAutowireEventHandler {

    override fun handleCommandInteractionAutowireEvent(
        session: CommandSession,
        parameter: KParameter,
        command: NexaCommand,
        result: arrow.core.Option<Any?>
    ): arrow.core.Option<Any?> {
        if (parameter.hasAnnotation<Autowired>()) {
            return Some(
                command.getNexaContext().getAuxContext().componentFactory().getComponent<ComponentProcessor>()
                    .autowire(parameter.type, parameter.annotations, mutableListOf())
            )
        }
        if (parameter.hasAnnotation<NexaCommand.Argument>()) {
            var handlerResult: arrow.core.Option<Any?> = None
            for (handler in command.getNexaContext().getAuxContext().componentFactory()
                .getComponents<CommandInteractionArgumentAutowireEventHandler>()) {
                handlerResult =
                    handler.handleCommandInteractionArgumentAutowireEvent(session, parameter, command, handlerResult)
            }
            return handlerResult
        }
        return result
    }

}

@Component
class CommandArgumentAutowireProcessor : CommandInteractionArgumentAutowireEventHandler {

    override fun handleCommandInteractionArgumentAutowireEvent(
        session: CommandSession,
        parameter: KParameter,
        command: NexaCommand,
        result: arrow.core.Option<Any?>
    ): arrow.core.Option<Any?> {
        val type = parameter.type.classifier
        if (type == null || type !is KClass<*>) return result
        return when (type) {
            ISession::class, CommandSession::class -> Some(session)
            else -> result
        }
    }

}
