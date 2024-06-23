package pixel.nexa.core.data.tag

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import de.undercouch.bson4jackson.BsonFactory
import pixel.nexa.core.util.ICopyable
import pixel.nexa.core.util.TypeUtils.convertIfNotOfType
import java.math.BigDecimal
import java.math.BigInteger


open class NumberTag<T : Number>(private val number: T) : ITag<T> {

    override fun toString(): String {
        return when (number) {
            is Long -> "${number}l"
            is Int -> "${number}i"
            is Float -> "${number.toBigDecimal().toPlainString()}f"
            is Byte -> "${number.toInt()}b"
            is Double -> "${number.toBigDecimal().toPlainString()}d"
            is Short -> "${number}s"
            is BigInteger -> "${number}I"
            is BigDecimal -> "${number.toPlainString()}D"
            else -> throw IllegalArgumentException("Invalid number type: ${number::class.java.name}")
        }
    }

    open fun bigIntValue(): BigInteger = read().convertIfNotOfType { it.toLong().toBigInteger() }
    open fun bigDecimalValue(): BigDecimal = read().convertIfNotOfType { it.toLong().toBigDecimal() }
    open fun byteValue(): Byte = read().convertIfNotOfType(Number::toByte)
    open fun shortValue(): Short = read().convertIfNotOfType(Number::toShort)
    open fun intValue(): Int = read().convertIfNotOfType(Number::toInt)
    open fun longValue(): Long = read().convertIfNotOfType(Number::toLong)
    open fun floatValue(): Float = read().convertIfNotOfType(Number::toFloat)
    open fun doubleValue(): Double = read().convertIfNotOfType(Number::toDouble)
    open fun booleanValue(): Boolean = longValue() != 0L

    override fun read(): T = number
    override fun copy() = NumberTag(number)

}

class LongTag(number: Long) : NumberTag<Long>(number)
class ShortTag(number: Short) : NumberTag<Short>(number)
class ByteTag(number: Byte) : NumberTag<Byte>(number)
class IntTag(number: Int) : NumberTag<Int>(number)
class FloatTag(number: Float) : NumberTag<Float>(number)
class DoubleTag(number: Double) : NumberTag<Double>(number)
class BigIntTag(number: BigInteger) : NumberTag<BigInteger>(number)
class BigDecimalTag(number: BigDecimal) : NumberTag<BigDecimal>(number)

class ListTag(vararg init: ITag<*>, private val list: MutableList<ITag<*>> = mutableListOf(*init)) : ITag<List<*>>,
    MutableList<ITag<*>> by list {

    override fun read(): List<*> = this.map { it.read() }
    override fun copy() = ListTag().apply { addAll(this.map { it.copy() }) }

    override fun toString(): String {
        var string = "["
        for (i in this) {
            val fallBack = string
            try {
                string += i.toString()
                string += ","
            } catch (throwable: Throwable) {
                if (throwable !is StackOverflowError) string = fallBack
                else throw StackOverflowError()
            }
        }
        return string.removeSuffix(",") + "]"
    }

}

enum class BooleanTag(private val value: Boolean) : ITag<Boolean> {

    TRUE(true), FALSE(false);

    override fun copy() = this

    override fun read() = value

}

class UnknownTag(private val value: Any?) : ITag<Any?> {
    override fun copy() = UnknownTag(if (value is ICopyable<*>) value.copy() else value)
    override fun read() = value

    override fun toString() = if (value is ITaggable<*>) value.toString() else "unknown"
}

class NullTag : ITag<Any?> {

    override fun copy() = NullTag()
    override fun read() = null

    override fun toString() = "null"

}

class StringTag(private val value: String) : ITag<String> {

    override fun copy() = StringTag(value)
    override fun read() = value

    override fun toString(): String = jacksonObjectMapper().writeValueAsString(read())

}

object Tags {

    fun fromValue(value: Any?): ITag<*> {
        if (value is ITag<*>) return value
        if (value is ITaggable<*>) return fromValue(value.toTag())
        if (value == null) return NullTag()
        if (value is String) return StringTag(value)
        if (value is Boolean) return if (value) BooleanTag.TRUE else BooleanTag.FALSE
        if (value is Long) return LongTag(value)
        if (value is Short) return ShortTag(value)
        if (value is Byte) return ByteTag(value)
        if (value is Int) return IntTag(value)
        if (value is Float) return FloatTag(value)
        if (value is Double) return DoubleTag(value)
        if (value is BigDecimal) return BigDecimalTag(value)
        if (value is BigInteger) return BigIntTag(value)
        if (value is Number) return NumberTag(value)
        if (value is Map<*, *>) return CompoundTag().putAll(value.mapValues { fromValue(it.value) }
            .mapKeys { it.key.toString() }.entries.map { it.key to it.value })
        if (value is Iterable<*>) return ListTag(list = value.map { fromValue(it) }.toMutableList())
        try {
            return ObjectMapper(BsonFactory()).let {
                it.readValue(it.writeValueAsBytes(value), jacksonTypeRef<Map<String, *>>())
            }.let(Tags::fromValue)
        } catch (_: Throwable) {
        }
        return UnknownTag(value)
    }

}

