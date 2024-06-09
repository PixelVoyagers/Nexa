package pixel.nexa.application

class NexaEngine

object NexaEngineBootstrap {

    @JvmStatic
    fun main(vararg args: String) {
        NexaApplicationBuilder()
            .applicationBuilder {
                name("Nexa").target<NexaEngine>()
            }
            .build()
            .run(*args)
    }

}