package pixel.nexa.core.web

import io.ktor.server.response.*
import io.ktor.server.routing.*
import pixel.auxframework.component.annotation.Autowired
import pixel.auxframework.core.registry.Identifier
import pixel.auxframework.core.registry.identifierOf
import pixel.auxframework.web.annotation.Path
import pixel.auxframework.web.annotation.QueryVariable
import pixel.auxframework.web.annotation.RequestMapping
import pixel.auxframework.web.annotation.RestController
import pixel.auxframework.web.server.ServerConfig
import pixel.nexa.core.NexaCore
import pixel.nexa.core.resource.AssetsMap
import java.net.URI

@RestController(Path("/nexa/core/resource"))
class NexaResourceWeb(private val serverCore: ServerConfig) {

    @Autowired
    private lateinit var assetsMap: AssetsMap

    fun getResourceUrl(identifier: Identifier): URI = URI.create("${serverCore.getServerPrefix()}/nexa/core/resource/assets?path=$identifier")


    @RequestMapping(Path("/assets"))
    suspend fun assets(ctx: RoutingContext, @QueryVariable path: String) = ctx.call.respondBytes {
        assetsMap.getOrNull(identifierOf(path, NexaCore.DEFAULT_NAMESPACE))?.stream()?.readAllBytes()!!
    }

}