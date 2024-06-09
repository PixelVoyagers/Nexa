package pixel.nexa.core.resource

import pixel.auxframework.core.registry.Identifier
import java.io.InputStream
import java.nio.charset.Charset

/**
 * 资源
 */
abstract class NexaResource {

    /**
     * 获取资源加载器
     */
    abstract fun getResourceLoader(): AbstractResourceLoader?
    abstract fun getResourceLocation(): Identifier
    abstract fun getResourceType(): Identifier
    abstract fun getResourcePath(): String
    abstract fun stream(): InputStream
    open fun getFileName(extension: Boolean = true) =
        getResourcePath().split("\\").joinToString("/").split("/").last().let {
            if (extension) it
            else it.split(".").let { split -> if (split.size <= 1) split else split.subList(0, split.size - 1) }
                .joinToString(".")
        }

    open fun contentAsByteArray(): ByteArray = stream().readAllBytes()
    open fun contentAsString(charset: Charset = Charsets.UTF_8): String = contentAsByteArray().toString(charset)
    override fun toString() =
        "Resource[type=${getResourceType()}, path=${getResourcePath()}, location=${getResourceLocation()}]"

}
