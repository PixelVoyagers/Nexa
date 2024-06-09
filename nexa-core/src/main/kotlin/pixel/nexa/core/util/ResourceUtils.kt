package pixel.nexa.core.util

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import pixel.auxframework.util.ConfigUtils
import pixel.nexa.core.resource.NexaResource
import java.io.OutputStream


/**
 * 资源实用工具
 */
object ResourceUtils {

    /**
     * 解析为表
     */
    inline fun <reified K, reified V> resolveToMap(resource: NexaResource, type: ConfigUtils.ConfigType) =
        resolveToMap<K, V>(resource.contentAsByteArray(), type)

    /**
     * 解析为表
     */
    inline fun <reified K, reified V> resolveToMap(content: ByteArray, type: ConfigUtils.ConfigType): Map<K, V> {
        val typeReference = jacksonTypeRef<Map<K, V>>()
        return type.readAs(content, typeReference)
    }

    /**
     * 写入对象到输出流
     */
    fun <T> writeObject(outputStream: OutputStream, type: ConfigUtils.ConfigType, content: T) =
        outputStream.write(type.write(content))

    @Suppress("UNCHECKED_CAST")
    fun flattenStringMap(
        map: Map<String, Any?>,
        parentKey: String = "",
        result: MutableMap<String, String> = mutableMapOf()
    ): Map<String, String> {
        for ((key, value) in map) {
            val newKey = if (parentKey.isEmpty()) key else "$parentKey.$key"
            when (value) {
                is Map<*, *> -> {
                    flattenStringMap(value as Map<String, Any?>, newKey, result)
                }

                is Collection<*> -> {
                    value.forEachIndexed { index, item ->
                        if (item is Map<*, *>) {
                            flattenStringMap(item as Map<String, Any?>, "$newKey.$index", result)
                        } else {
                            result["$newKey.$index"] = item.toString()
                        }
                    }
                }

                else -> {
                    result[newKey] = value.toString()
                }
            }
        }
        return result
    }

}