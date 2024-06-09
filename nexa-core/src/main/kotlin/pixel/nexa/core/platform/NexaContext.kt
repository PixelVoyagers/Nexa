package pixel.nexa.core.platform

import pixel.auxframework.context.AuxContext
import pixel.nexa.core.platform.adapter.NexaAdapter

interface NexaContext {

    fun getAuxContext(): AuxContext
    fun getAdapters(): Set<NexaAdapter<*, *>>

    fun start() = getAdapters().filter(NexaAdapter<*, *>::isEnabled).forEach(NexaAdapter<*, *>::start)
    fun stop() = getAdapters().filter(NexaAdapter<*, *>::isEnabled).forEach(NexaAdapter<*, *>::stop)

}
