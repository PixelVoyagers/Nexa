package pixel.nexa.core.platform

import pixel.auxframework.component.factory.getComponents
import pixel.auxframework.context.AuxContext
import pixel.auxframework.util.FunctionUtils.memorize
import pixel.nexa.core.component.AfterNexaContextStarted
import pixel.nexa.core.component.NexaEventListener
import pixel.nexa.core.platform.adapter.NexaAdapter

interface NexaContext {

    fun isStarted(): Boolean

    fun getListeners(): Set<NexaEventListener> = {
        getAuxContext().componentFactory().getComponents<NexaEventListener>().toSet()
    }.let {
        if (isStarted()) memorize(isStarted(), this, getAuxContext()) { it() }
        else it()
    }

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

inline fun <reified T> NexaContext.getListenersOfType() = getListeners().filterIsInstance<T>().toSet()
