package pixel.nexa.core.web

import pixel.auxframework.core.AuxVersion
import pixel.auxframework.web.annotation.Path
import pixel.auxframework.web.annotation.RequestMapping
import pixel.auxframework.web.annotation.RestController
import pixel.auxframework.web.server.ServerConfig
import pixel.auxframework.web.util.AuxWebResponse
import pixel.nexa.core.NexaVersion
import pixel.nexa.core.platform.NexaContext

@RestController(Path("/nexa/core"))
class NexaCoreWeb(private val nexaContext: NexaContext) {

    data class Information(var aux: Aux = Aux(), var nexa: Nexa = Nexa()) {

        data class Aux(var version: AuxVersion = AuxVersion.current())

        data class Nexa(var version: NexaVersion = NexaVersion.current(), var context: Context = Context()) {

            data class Context(var adapters: MutableSet<Adapter> = mutableSetOf())
            data class Adapter(var platform: String, var bots: MutableSet<Bot> = mutableSetOf()) {
                data class Bot(var name: String)
            }

        }

    }

    @RequestMapping(Path("/information"))
    suspend fun mappingInformation(response: AuxWebResponse) = response.respondJson {
        Information().apply {
            nexa.context.adapters += nexaContext.getAdapters().map { adapter ->
                Information.Nexa.Adapter(adapter.getPlatform()).apply {
                    bots += adapter.getBots().map {
                        Information.Nexa.Adapter.Bot(it.getName())
                    }
                }
            }
        }
    }

}

fun ServerConfig.getServerPrefix() = "http://localhost:${port}"
