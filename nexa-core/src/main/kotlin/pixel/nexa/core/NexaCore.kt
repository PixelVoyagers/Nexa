package pixel.nexa.core

import pixel.auxframework.component.annotation.Component
import java.io.File
import java.nio.file.Path

@Component
class NexaCore {

    companion object {
        const val DEFAULT_NAMESPACE = "nexa"
    }

    fun getDirectory(vararg path: String): File =
        Path.of(System.getProperty("user.dir"), ".nexa", *path).toFile().also(File::mkdirs)

    fun getDirectoryFile(vararg path: String): File =
        Path.of(System.getProperty("user.dir"), ".nexa", *path).toFile().apply {
            parentFile.mkdirs()
        }

}
