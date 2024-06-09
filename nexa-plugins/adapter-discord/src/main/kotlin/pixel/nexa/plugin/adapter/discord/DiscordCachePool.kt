package pixel.nexa.plugin.adapter.discord

import kotlin.reflect.KClass

class DiscordCachePool {

    private val pool = mutableMapOf<KClass<*>, MutableMap<String, Any>>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(type: KClass<T>, name: String): T? = pool.getOrPut(type) { mutableMapOf() }[name] as? T
    inline fun <reified T : Any> get(name: String) = get(T::class, name)

    fun <T : Any> getOrPut(type: KClass<T>, name: String, put: () -> T): T {
        val typedPool = pool.getOrPut(type) { mutableMapOf() }
        if (name !in typedPool) typedPool[name] = put()
        return get(type, name)!!
    }

    inline fun <reified T : Any> getOrPut(name: String, noinline put: () -> T) = getOrPut(T::class, name, put)

}