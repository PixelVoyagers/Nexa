package pixel.nexa.core.web

import io.ktor.server.application.*
import io.ktor.server.websocket.*
import pixel.auxframework.component.annotation.Service
import pixel.auxframework.web.AuxWeb
import pixel.auxframework.web.server.InitializeWebServer

@Service
class NexaWebSockets(private val auxWeb: AuxWeb) : InitializeWebServer {

    override fun initializeWebServer() {
        auxWeb.getWebApplication().application.apply {
            install(WebSockets)
        }
    }

}
