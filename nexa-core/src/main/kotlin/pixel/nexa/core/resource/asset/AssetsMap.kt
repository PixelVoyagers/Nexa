package pixel.nexa.core.resource.asset

import pixel.auxframework.component.annotation.Autowired
import pixel.auxframework.component.annotation.Component
import pixel.auxframework.core.registry.Identifier
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.resource.ResourceLoader
import pixel.nexa.core.resource.ResourceMap
import pixel.nexa.core.web.NexaResourceWeb

@Component
class AssetsMap(private val pageViewEngine: PageViewEngine, private val textureEngine: TextureEngine, private val nexaResourceWeb: NexaResourceWeb) :
    ResourceMap() {

    @Autowired
    private lateinit var loader: ResourceLoader

    fun getTexture(identifier: Identifier) = textureEngine.getTexture(identifier)
    fun getTexture(type: String, identifier: Identifier, suffix: String = ".png") =
        getTexture(identifierOf("${identifier.getNamespace()}:textures/$type/${identifier.getPath()}$suffix"))

    fun getPage(identifier: Identifier) = pageViewEngine.getPage(identifier)

}