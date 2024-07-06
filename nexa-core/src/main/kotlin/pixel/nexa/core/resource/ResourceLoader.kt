package pixel.nexa.core.resource

import com.google.common.collect.HashBiMap
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import pixel.auxframework.component.annotation.Component
import pixel.auxframework.core.registry.Identifier
import java.util.*


abstract class AbstractResourceLoader {

    fun loadAsResourceMap(path: String, resolver: ResourcePatternResolver): Map<Identifier, List<NexaResource>> {
        val resourceMap = hashMapOf<Identifier, MutableList<NexaResource>>()
        for (resource in load(path, resolver)) {
            resourceMap.getOrPut(resource.getResourceType(), ::mutableListOf).add(resource)
        }
        return Collections.unmodifiableMap(resourceMap.mapValues { Collections.unmodifiableList(it.value) })
    }

    fun load(path: String = "", resolver: ResourcePatternResolver): Sequence<NexaResource> = sequence {
        val baseDir = resolver.getResource(path)
        val resolved = runCatching {
            resolver.getResources("$path/**/*.*")
        }.getOrNull() ?: emptyArray()
        for (resource in resolved) {
            if (!resource.isReadable) continue
            try {
                val resourcePath = resource
                    .uri.toString().removePrefix(baseDir.uri.toString())
                    .split("\\").joinToString("/").split("/")
                    .let { it.subList(1, it.size) }
                    .toMutableList()
                if (resourcePath.size < 2) continue
                if (resourcePath.size == 2) resourcePath[1].let {
                    resourcePath[1] = "root"
                    resourcePath += it
                }
                val resourceLocation =
                    Identifier(resourcePath[0], resourcePath.subList(1, resourcePath.size).joinToString("/"))
                val resourceType = Identifier(resourcePath[0], resourcePath[1])
                val resourceFilePath = resourcePath.subList(2, resourcePath.size).joinToString("/")
                yield(
                    object : NexaResource() {
                        override fun getResourceLoader() = this@AbstractResourceLoader
                        override fun getResourceLocation() = resourceLocation
                        override fun getResourcePath() = resourceFilePath
                        override fun getResourceType() = resourceType
                        override fun stream() = resource.inputStream
                    }
                )
            } catch (_: Throwable) {
            }
        }
    }

    fun load(path: String = "", resourceLoader: org.springframework.core.io.ResourceLoader) =
        load(path = path, resolver = PathMatchingResourcePatternResolver(resourceLoader))

    fun load(path: String = "", classLoader: ClassLoader) =
        load(path = path, resolver = PathMatchingResourcePatternResolver(classLoader))

}

@Component
class ResourceLoader : AbstractResourceLoader()

open class ResourceLocationMap<T> {
    protected val pool: HashBiMap<Identifier, T> = HashBiMap.create()
    operator fun set(key: Identifier, value: T) = this.also { pool[key] = value }
    operator fun set(key: T, value: Identifier) = this.also { pool.inverse()[key] = value }

    operator fun get(key: Identifier) = getOrNull(key)!!
    operator fun get(key: T) = getOrNull(key)!!

    open fun getOrNull(key: Identifier) = pool[key]
    open fun getOrNull(key: T) = pool.inverse()[key]

    operator fun contains(key: Identifier) = pool.containsKey(key)
    operator fun contains(value: T) = pool.containsValue(value)
    operator fun minusAssign(value: T) {
        pool.inverse().remove(value)
    }

    operator fun minusAssign(value: Identifier) {
        pool.remove(value)
    }

    fun entries() = pool.entries
    fun keys() = pool.keys
    fun values() = pool.values
}

open class ResourceMap : ResourceLocationMap<NexaResource>() {

    fun load(map: Map<Identifier, List<NexaResource>>) {
        for ((_, resources) in map) {
            for (resource in resources) {
                this[resource.getResourceLocation()] = resource
            }
        }
    }

}

