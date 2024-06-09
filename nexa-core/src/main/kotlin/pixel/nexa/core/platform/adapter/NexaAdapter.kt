package pixel.nexa.core.platform.adapter

import pixel.nexa.core.platform.NexaContext

interface NexaAdapter<T : NexaBot<T>, C : AbstractNexaAdapter.Companion.Config> {

    fun getPlatform(): String
    fun getContext(): NexaContext

    fun getBots(): Set<T>

    fun start() = getBots().forEach(NexaBot<*>::start)
    fun stop() = getBots().forEach(NexaBot<*>::stop)

    fun getConfig(): C
    fun isEnabled(): Boolean


    fun getBotInternalName(bot: NexaBot<*>) = "${getPlatform()}:${bot.getName()}"

}