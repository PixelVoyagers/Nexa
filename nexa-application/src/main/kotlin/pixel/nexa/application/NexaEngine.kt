package pixel.nexa.application

import pixel.auxframework.context.AuxContext
import pixel.auxframework.core.AuxVersion
import pixel.auxframework.web.AuxWeb
import pixel.nexa.plugin.adapter.discord.DiscordAdapter
import pixel.nexa.plugin.profile.IdCardCommand

class NexaEngine

object NexaEngineBootstrap {

    @JvmStatic
    fun main(vararg args: String) {
        NexaApplicationBuilder()
            .applicationBuilder {
                name("Nexa").target<NexaEngine>().also {
                    it.context?.classLoaders?.apply {
                        this += arrayOf(
                            AuxWeb::class.java.classLoader,
                            AuxContext::class.java.classLoader,
                            AuxVersion::class.java.classLoader,
                            IdCardCommand::class.java.classLoader
                        )
                        this += arrayOf(DiscordAdapter::class.java.classLoader)
                    }
                }
            }
            .build()
            .run(*args)
    }

}