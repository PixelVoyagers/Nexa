package pixel.nexa.core.web

import com.microsoft.playwright.Route
import pixel.auxframework.core.AuxVersion
import pixel.auxframework.web.annotation.Path
import pixel.auxframework.web.annotation.RequestMapping
import pixel.auxframework.web.annotation.RestController
import pixel.auxframework.web.server.ServerConfig
import pixel.auxframework.web.util.AuxWebResponse
import pixel.nexa.core.NexaVersion
import pixel.nexa.core.platform.NexaContext
import pixel.nexa.core.util.BrowserUtils.context
import java.util.regex.Pattern

@RestController(Path("/nexa/core"))
class NexaCoreWeb(private val nexaContext: NexaContext, serverConfig: ServerConfig) {

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

    init {
        context.route(Pattern.compile("^nexa://internal/.*")) {
            val uri = serverConfig.getServerPrefix() + it.request().url().removePrefix("nexa://internal")
            val request = it.request()
            val response = it.fetch(
                Route.FetchOptions()
                    .setUrl(uri)
                    .setHeaders(request.headers())
                    .setPostData(request.postData())
                    .setMethod(request.method())
            )
            it.fulfill(Route.FulfillOptions().setResponse(response))
        }
    }

}

fun ServerConfig.getServerPrefix() = "http://localhost:${port}"
