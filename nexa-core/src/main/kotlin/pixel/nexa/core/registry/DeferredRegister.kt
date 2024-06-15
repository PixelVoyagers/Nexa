package pixel.nexa.core.registry

import com.google.common.collect.HashBiMap
import pixel.auxframework.component.annotation.Component
import pixel.auxframework.component.factory.getComponent
import pixel.auxframework.core.registry.Identifier
import pixel.auxframework.core.registry.identifierOf
import pixel.auxframework.plugin.loader.AuxPluginClassLoader
import pixel.nexa.core.NexaCore

@Component
class DeferredRegistry {

    val instances = mutableSetOf<DeferredRegister<*>>()

}

class DeferredRegister<T>(private val registry: NexaRegistry<T>, pluginId: String? = null) {

    inner class Entry(private val name: Identifier) {

        fun getLocation() = name
        fun getOrNull() = registry.get(name)
        fun get() = getOrNull()!!

    }

    fun execute() {
        for (entry in store) {
            if (entry.value.second) continue
            registry.register(entry.key, entry.value.first)
            entry.setValue(entry.value.first to true)
        }
    }

    fun getRegistry() = registry

    init {
        registry
            .getContext()
            .getAuxContext()
            .componentFactory()
            .getComponent<DeferredRegistry>()
            .instances
            .add(this)
    }

    private val pluginId: String = runCatching {
        (Class.forName(Thread.currentThread().stackTrace[2].className).classLoader as AuxPluginClassLoader).plugin.getPluginMetadata().getName()
    }.getOrNull() ?: pluginId ?: NexaCore.DEFAULT_NAMESPACE

    private val store = HashBiMap.create<Identifier, Pair<() -> T, Boolean>>()

    fun register(name: Identifier, value: () -> T): Entry {
        if (name in store) throw IllegalStateException("Duplicate name: $name")
        store[name] = value to false
        return Entry(name)
    }

    fun register(name: Identifier, value: T) = register(name) { value }

    fun register(name: String, value: () -> T) = register(identifierOf(name, pluginId), value)

    fun register(name: String, value: T) = register(name) { value }

}
