package pixel.nexa.core.service

import pixel.auxframework.component.annotation.Service
import pixel.auxframework.util.ConfigUtils
import pixel.auxframework.util.toJavaClass
import pixel.auxframework.util.toParameterized
import pixel.nexa.core.NexaCore
import pixel.nexa.core.platform.adapter.AbstractNexaAdapter
import java.nio.file.Path
import kotlin.io.path.pathString

@Service
class NexaConfig(private val nexaCore: NexaCore) {

    fun getAdapterConfig(adapter: AbstractNexaAdapter<*, *>): AbstractNexaAdapter.Companion.Config {
        val name = "${adapter.getPlatform()}.yml"
        val fileType = ConfigUtils.ConfigTypes.YAML
        val type = adapter::class.java.genericSuperclass.toParameterized().actualTypeArguments[1].toJavaClass()
        return with(nexaCore.getDirectoryFile("config/adapters").toPath()) {
            val file = Path.of(pathString, name).toFile()
            file.parentFile.mkdirs()
            if (!file.exists()) {
                file.createNewFile()
                file.writeBytes(fileType.default(type))
            }
            fileType.readAs(file.readBytes(), type)
        } as AbstractNexaAdapter.Companion.Config
    }

}
