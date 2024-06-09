package pixel.nexa.plugin.adapter.discord

import com.neovisionaries.ws.client.WebSocketFactory
import dev.minn.jda.ktx.jdabuilder.light
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.requests.GatewayIntent
import okhttp3.OkHttpClient
import pixel.nexa.core.platform.adapter.AbstractNexaBot
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI

class DiscordBot(private val discordAdapter: DiscordAdapter, private val config: Map<String, Any>) :
    AbstractNexaBot<DiscordBot>() {

    override fun getAdapter() = discordAdapter

    private lateinit var botInstance: JDA

    fun login() = light(config["token"].toString(), true) {
        if ("proxy" in config) {
            val proxyUri = URI.create(config["proxy"].toString())
            setWebsocketFactory(WebSocketFactory().apply { proxySettings.setServer(proxyUri) })
            setHttpClientBuilder(
                OkHttpClient.Builder().proxy(
                    Proxy(
                        Proxy.Type.entries.first { it.name.lowercase() == proxyUri.scheme },
                        InetSocketAddress(proxyUri.host, proxyUri.port)
                    )
                )
            )
        }
        if ("intents" in config) enableIntents(GatewayIntent.getIntents(config["intents"].toString().toInt()))
    }

    override fun start() {
        botInstance = login()
        botInstance.awaitReady()
    }

    override fun stop() {
        botInstance.awaitShutdown()
    }

}