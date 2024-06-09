package pixel.nexa.core.web

import io.ktor.server.response.*
import io.ktor.server.routing.*
import pixel.auxframework.core.registry.identifierOf
import pixel.auxframework.web.annotation.Path
import pixel.auxframework.web.annotation.QueryVariable
import pixel.auxframework.web.annotation.RequestMapping
import pixel.auxframework.web.annotation.RestController
import pixel.nexa.core.NexaCore
import pixel.nexa.core.resource.AssetsMap

@RestController(Path("/nexa/core/resource"))
class NexaResourceWeb(val assetsMap: AssetsMap) {

    @RequestMapping(Path("/assets"))
    suspend fun assets(ctx: RoutingContext, @QueryVariable path: String) = ctx.call.respondBytes {
        assetsMap.getOrNull(identifierOf(path, NexaCore.DEFAULT_NAMESPACE))?.stream()?.readAllBytes()!!
    }

}