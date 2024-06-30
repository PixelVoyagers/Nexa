package pixel.nexa.core.data.tag

import java.util.*

class CompoundTag : ITag<Map<String, Any?>> {

    private val map: MutableMap<String, ITag<*>> = mutableMapOf()

    fun clear() = map.clear()

    operator fun set(name: String, tag: ITag<*>) = put(name, tag)
    operator fun plusAssign(pairs: Iterable<Pair<String, ITag<*>>>) {
        pairs.forEach { this += it }
    }

    operator fun plusAssign(pair: Pair<String, ITag<*>>) {
        set(pair.first, pair.second)
    }

    fun put(name: String, tag: ITag<*>) = this.also { map[name] = tag }
    fun putAll(iterable: Iterable<Pair<String, ITag<*>>>) = this.also {
        iterable.forEach { map[it.first] = it.second }
    }

    operator fun contains(name: String) = map.contains(name)
    operator fun get(name: String): ITag<*>? = map[name]

    override fun copy(): ITag<Map<String, Any?>> = CompoundTag().apply {
        putAll(this@CompoundTag.map.mapValues { it.value.copy() }.entries.map { it.key to it.value })
    }

    override fun read(): Map<String, Any?> = Collections.unmodifiableMap(map.toMap().mapValues { it.value.read() })

    fun getEntries() = map.entries

    fun getBoolean(name: String): Boolean? = get(name)?.let {
        when (it) {
            is StringTag -> it.read() == "true"
            is NumberTag -> it.booleanValue()
            is BooleanTag -> it.read()
            else -> false
        }
    }

    fun putBoolean(name: String, value: Boolean) = put(name, if (value) BooleanTag.TRUE else BooleanTag.FALSE)

    fun getString(name: String): String? = get(name)?.let {
        when (it) {
            is StringTag -> it.read()
            else -> it.read().toString()
        }
    }

    fun putString(name: String, value: String) = put(name, StringTag(value))

    fun isNull(name: String) = get(name) is NullTag || name !in this
    fun putNull(name: String) = put(name, NullTag())

    fun getList(name: String) = get(name) as? ListTag
    fun putList(name: String, tag: ListTag) = put(name, tag)

    fun getCompound(name: String) = get(name) as? CompoundTag
    fun putCompound(name: String, tag: CompoundTag) = put(name, tag)
    fun putCompound(name: String, map: Map<String, *>) = put(
        name,
        CompoundTag().also { it.putAll(map.mapValues(Tags::fromValue).entries.map { entry -> entry.key to entry.value }) }
    )

    fun getNumber(name: String) = (get(name) as? NumberTag)?.read()
    fun putNumber(name: String, number: Number) = put(name, NumberTag(number))

    fun getLong(name: String) = (get(name) as? NumberTag)?.longValue()
    fun getInt(name: String) = (get(name) as? NumberTag)?.intValue()
    fun getShort(name: String) = (get(name) as? NumberTag)?.shortValue()
    fun getByte(name: String) = (get(name) as? NumberTag)?.byteValue()
    fun getFloat(name: String) = (get(name) as? NumberTag)?.floatValue()
    fun getDouble(name: String) = (get(name) as? NumberTag)?.doubleValue()
    fun getBigInteger(name: String) = (get(name) as? NumberTag)?.bigIntValue()
    fun getBigDecimal(name: String) = (get(name) as? NumberTag)?.bigDecimalValue()

    override fun toString(): String {
        var string = "{"
        for (i in getEntries()) {
            val fallBack = string
            try {
                string += StringTag(i.key).toString()
                string += ":"
                string += i.value.toString()
                string += ","
            } catch (throwable: Throwable) {
                if (throwable !is StackOverflowError) string = fallBack
                else throw StackOverflowError()
            }
        }
        return string.removeSuffix(",") + "}"
    }

    override fun hashCode() = map.hashCode()
    override fun equals(other: Any?) =
        other === this || (other != null && other is CompoundTag && other.hashCode() == hashCode())

    fun remove(name: String) = map.remove(name)

}

fun compoundTagOf(vararg tags: Pair<String, ITag<*>>) = CompoundTag().putAll(tags.toList())
fun compoundTagOf(tags: Map<String, Any?>) = Tags.fromValue(tags) as CompoundTag
