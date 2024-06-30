package pixel.nexa.core.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import de.undercouch.bson4jackson.BsonFactory
import pixel.auxframework.util.ConfigUtils
import pixel.nexa.core.NexaCore
import java.io.File
import java.nio.file.Path

enum class DataTypes(private val mapper: () -> ObjectMapper) : ConfigUtils.ConfigType {

    BSON({ ObjectMapper(BsonFactory()) });

    override fun <T> write(value: T): ByteArray = mapper().writeValueAsBytes(value)

    override fun <T : Any> readAs(bytes: ByteArray, typeReference: TypeReference<T>): T =
        mapper().readValue(bytes, typeReference)

}

/**
 * 存储对象
 */
interface IStorage<T> {

    /**
     * 读取
     */
    fun get(): T

    /**
     * 写入
     */
    fun set(value: T)

    /**
     * 重置
     */
    fun reset(): Unit = Unit.apply { getDefault()?.let(::set) }

    /**
     * 获取默认对象
     */
    fun getDefault(): T?

    /**
     * 刷新
     */
    fun flush() {}

}

/**
 * 内存存储
 */
class MemoryStorage<T>(private val default: () -> T? = { null }) : IStorage<T> {

    private var value: T? = null

    override fun get(): T {
        if (value == null) reset()
        return value!!
    }

    override fun set(value: T) {
        this.value = value
    }

    override fun getDefault() = default()

}

/**
 * 文件存储
 */
class FileStorage<T : Any>(
    private val typeRef: TypeReference<T>,
    private val default: () -> T? = { null },
    private val file: File,
    private val type: ConfigUtils.ConfigType = ConfigUtils.ConfigTypes.YAML
) : IStorage<T> {

    constructor(
        typeRef: TypeReference<T>,
        default: () -> T? = { null },
        path: Path,
        type: ConfigUtils.ConfigType = ConfigUtils.ConfigTypes.YAML
    ) : this(typeRef, default, path.toFile(), type)

    constructor(
        typeRef: TypeReference<T>,
        default: () -> T? = { null },
        path: String,
        core: NexaCore,
        type: ConfigUtils.ConfigType = ConfigUtils.ConfigTypes.YAML
    ) : this(typeRef, default, core.getDirectoryFile("data", path), type)

    override fun getDefault() = default()

    override fun get(): T {
        if (!file.exists()) reset()
        return type.readAs(file.readBytes(), typeRef)
    }

    override fun reset() {
        flush()
        super.reset()
    }

    override fun set(value: T) = Unit.also {
        file.writeBytes(type.write(value))
    }

    fun getFile() = file

    override fun flush() {
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        if (file.readBytes().isEmpty()) super.reset()
    }

    override fun hashCode() = file.absolutePath.hashCode()
    override fun equals(other: Any?) = other === this || (other != null && (other is FileStorage<*> && other.hashCode() == hashCode()))

}
