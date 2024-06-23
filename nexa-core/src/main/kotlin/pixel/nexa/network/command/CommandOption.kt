package pixel.nexa.network.command

import arrow.core.Some
import pixel.auxframework.component.annotation.Component
import pixel.auxframework.core.registry.Identifier
import pixel.auxframework.util.toClass
import pixel.nexa.network.message.MessageFragment
import pixel.nexa.network.message.MessageFragments
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubclassOf


object OptionTypes {

    const val UNKNOWN = "unknown"
    const val ANY = "any"
    const val STRING = "string"
    const val NUMBER = "number"
    const val INTEGER = "integer"
    const val BOOLEAN = "boolean"
    const val USER = "user"
    const val CHANNEL = "channel"
    const val ROLE = "role"
    const val ATTACHMENT = "attachment"
    const val MENTION = "mention"

}

/**
 * 选项
 */
open class Option(
    private val command: NexaCommand,
    private val name: String,
    private val type: Identifier,
    private val autoCompleteMode: NexaCommand.Option.AutoCompleteMode,
    private val isRequired: Boolean,
    private var receiverType: KClass<*>? = null
) {

    fun getReceiverType() = receiverType

    /**
     * 获取指令
     */
    open fun getCommand(): NexaCommand = command

    /**
     * 获取名称
     */
    open fun getName(): String = name

    /**
     * 获取类型
     */
    open fun getType(): Identifier = type

    /**
     * 获取自动完成模式
     */
    open fun getAutoCompleteMode(): NexaCommand.Option.AutoCompleteMode = autoCompleteMode

    /**
     * 是否必须填入
     */
    open fun isRequired(): Boolean = isRequired

}

/**
 * 选项映射
 */
interface OptionMapping {

    /**
     * 获取选项
     */
    fun getOption(): Option?

    /**
     * 转为 [String]
     */
    fun asString(): String

    /**
     * 转为 [MessageFragment]
     */
    fun getMessageFragment(): MessageFragment = MessageFragments.text(asString())

    /**
     * 转为 [Double]
     */
    fun asNumber(): Double = asString().toDouble()

    /**
     * 转为 [BigDecimal]
     */
    fun asBigDecimal(): BigDecimal = asString().toBigDecimal()

    /**
     * 转为 [BigInteger]
     */
    fun asBigInteger(): BigInteger = asString().toBigInteger()

    /**
     * 转为 [Long]
     */
    fun asInteger(): Long = asString().toLong()

    /**
     * 转为 [Boolean]
     */
    fun asBoolean(): Boolean = asString().toIntOrNull() != 0 && asString() != "false"

}

@Component
class NexaCommandOptionAutowireProcessor : CommandInteractionOptionAutowireEventHandler {
    @Suppress("UNCHECKED_CAST")
    override fun handleCommandInteractionOptionAutowireEvent(
        session: CommandSession,
        parameter: KParameter,
        optionMapping: OptionMapping,
        command: NexaCommand,
        result: arrow.core.Option<Any?>
    ): arrow.core.Option<Any?> {
        val returnResult: Any? = when (val type = parameter.type.toClass()) {
            Int::class, java.lang.Integer::class -> optionMapping.asInteger().toInt()
            Long::class, java.lang.Long::class -> optionMapping.asInteger()
            Float::class, java.lang.Float::class -> optionMapping.asNumber().toFloat()
            Double::class, java.lang.Double::class -> optionMapping.asNumber()
            Short::class, java.lang.Short::class -> optionMapping.asInteger().toShort()
            Byte::class, java.lang.Byte::class -> optionMapping.asInteger().toByte()
            Char::class, java.lang.Character::class -> optionMapping.asString().getOrNull(0)
            Boolean::class, java.lang.Boolean::class -> optionMapping.asBoolean()
            BigDecimal::class -> optionMapping.asBigDecimal()
            BigInteger::class -> optionMapping.asBigDecimal().toBigInteger()
            String::class -> optionMapping.asString()
            OptionMapping::class -> optionMapping
            if (type.isSubclassOf(Enum::class)) type else Void::class -> (type as KClass<Enum<*>>).java.enumConstants.firstOrNull { it.name == optionMapping.asString() }
            else -> result
        }
        return Some(returnResult)
    }

}
