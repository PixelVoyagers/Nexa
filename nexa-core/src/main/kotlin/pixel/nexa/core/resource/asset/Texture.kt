package pixel.nexa.core.resource.asset

import pixel.auxframework.component.annotation.Autowired
import pixel.auxframework.component.annotation.Component
import pixel.auxframework.core.registry.Identifier
import pixel.nexa.core.resource.NexaResource
import pixel.nexa.core.resource.NexaResourceLike
import pixel.nexa.core.web.NexaResourceWeb
import java.awt.Color
import java.awt.image.BufferedImage

@Component
class TextureEngine(internal val nexaResourceWeb: NexaResourceWeb) {

    @Autowired internal lateinit var assetsMap: AssetsMap

    fun getTexture(identifier: Identifier) = Texture(this, assetsMap[identifier])

}

class Texture(private val engine: TextureEngine, private val resource: NexaResource) : NexaResourceLike {

    override fun asResource() = resource

    fun asUrl() = engine.nexaResourceWeb.getResourceUrl(engine.assetsMap[resource])

}

object TextureUtils {

    fun adjustImageTone(image: BufferedImage, newColor: Color) {
        val width = image.width
        val height = image.height
        for (y in 0 until height) {
            for (x in 0 until width) {
                val argb = image.getRGB(x, y)
                val rgb = argb or -0x1000000
                val alpha = (argb shr 24) and 0xFF
                var red = (rgb shr 16) and 0xFF
                var green = (rgb shr 8) and 0xFF
                var blue = rgb and 0xFF
                red = (red * 0.5 + newColor.red * 0.5).toInt()
                green = (green * 0.5 + newColor.green * 0.5).toInt()
                blue = (blue * 0.5 + newColor.blue * 0.5).toInt()
                val newArgb = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
                image.setRGB(x, y, newArgb)
            }
        }
    }

}