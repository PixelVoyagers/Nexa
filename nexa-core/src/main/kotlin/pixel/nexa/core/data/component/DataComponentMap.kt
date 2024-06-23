package pixel.nexa.core.data.component

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import pixel.auxframework.core.registry.Identifier
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.NexaCore
import pixel.nexa.core.data.tag.CompoundTag
import pixel.nexa.core.data.tag.ITag

class DataComponent<E, T : ITag<*>>(
    private val map: DataComponentMap,
    private val name: Identifier,
    private val type: IDataComponentType<E, T>,
    private var value: Option<E>
) {

    fun set(value: Option<E>) {
        this.value = value
    }

    fun getName() = name
    fun getType() = type
    fun getMap() = map
    fun get() = value

    fun forget() = map.getData().remove(type)

}

class DataComponentMap {

    val schema = mutableMapOf<Identifier, IDataComponentType<*, *>>()
    private val data = mutableMapOf<IDataComponentType<*, *>, DataComponent<*, *>>()

    fun getData() = data

    @Suppress("UNCHECKED_CAST")
    fun load(tag: CompoundTag) = apply {
        data.clear()
        for (item in tag.getEntries()) {
            val name = identifierOf(item.key, NexaCore.DEFAULT_NAMESPACE)
            if (name !in schema) {
                tag.remove(item.key)
                continue
            }
            val type = schema[name]!! as IDataComponentType<Any?, ITag<Any?>>
            data[type] = DataComponent(this, name, type, Some(type.deserialize(item.value as ITag<Any?>)))
        }
        for (entry in schema) {
            if (entry.value !in data) {
                data[entry.value] = DataComponent(this, entry.key, entry.value, None)
            }
        }
    }

    operator fun contains(name: Identifier) = data.values.firstOrNull { it.getName() == name } != null
    operator fun contains(type: IDataComponentType<*, *>) = type in data


    fun <E, T : ITag<*>> put(type: IDataComponentType<E, T>, value: E) = value.apply {
        get(type)?.set(Some(value))
    }

    fun <E, T : ITag<*>> put(name: Identifier, value: E) = value.apply {
        get<E, T>(name)?.set(Some(value))
    }

    fun <E, T : ITag<*>> remove(type: IDataComponentType<E, T>) {
        get(type)?.set(None)
    }

    fun <E, T : ITag<*>> remove(name: Identifier) {
        get<E, T>(name)?.set(None)
    }

    @Suppress("UNCHECKED_CAST")
    fun <E, T : ITag<*>> get(name: Identifier) =
        data.values.firstOrNull { it.getName() == name } as? DataComponent<E, T>

    @Suppress("UNCHECKED_CAST")
    fun <E, T : ITag<*>> get(type: IDataComponentType<E, T>) = data[type] as? DataComponent<E, T>

    fun <E, T : ITag<*>> getTyped(type: IDataComponentType<E, T>) = get(type)?.get()
    fun <E, T : ITag<*>> getTyped(type: Identifier) = get<E, T>(type)?.get()

    fun <E, T : ITag<*>> getOrPut(type: IDataComponentType<E, T>, default: () -> E): E {
        val got = getTyped(type)
        return if (got == null || got == None) put(type, default())
        else got.getOrNull()!!
    }

    fun <E, T : ITag<*>> getOrPut(type: Identifier, default: () -> E): E {
        val got = getTyped<E, T>(type)
        return if (got == null || got == None) put<E, T>(type, default())
        else got.getOrNull()!!
    }

    @Suppress("UNCHECKED_CAST")
    fun read(): CompoundTag {
        val tag = CompoundTag()
        for (entry in data) {
            val type = entry.value.getType() as IDataComponentType<Any?, ITag<Any?>>
            val got = entry.value.get()
            if (got != None)
                tag[entry.value.getName().toString()] = type.serialize(got.getOrNull())
        }
        return tag
    }

    override fun hashCode() = read().hashCode()
    override fun equals(other: Any?) =
        other === this || (other != null && other is DataComponentMap && other.hashCode() == hashCode())

    fun isEmpty() = data.isEmpty() || data.all { it.value.get() == None }

}