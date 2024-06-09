package pixel.nexa.core.platform

import pixel.auxframework.component.factory.getComponents
import pixel.auxframework.context.AuxContext
import pixel.nexa.core.component.AfterNexaContextStarted
import pixel.nexa.core.platform.adapter.NexaAdapter

interface NexaContext {

    fun getAuxContext(): AuxContext
    fun getAdapters(): Set<NexaAdapter<*, *>>

    fun start() {
        getAdapters().filter(NexaAdapter<*, *>::isEnabled).forEach(NexaAdapter<*, *>::start)
        getAuxContext().componentFactory().getComponents<AfterNexaContextStarted>().forEach {
            it.afterNexaContextStarted(this)
        }
    }

    fun stop() = getAdapters().filter(NexaAdapter<*, *>::isEnabled).forEach(NexaAdapter<*, *>::stop)

}
