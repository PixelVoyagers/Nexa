package pixel.nexa.application

import pixel.auxframework.component.factory.getComponents
import pixel.nexa.core.annotation.Adapter
import pixel.nexa.core.platform.NexaContext
import pixel.nexa.core.platform.adapter.NexaAdapter
import kotlin.reflect.full.findAnnotation

class ApplicationNexaContext : NexaContext {

    private var isStarted = false
    override fun isStarted() = isStarted

    internal lateinit var application: NexaApplication
    fun getApplication() = application

    override fun getAuxContext() = application.context

    override fun getAdapters() = getAuxContext().componentFactory()
        .getComponents<NexaAdapter<*, *>>()
        .filter { it::class.findAnnotation<Adapter>() != null }
        .toSet()

    override fun start() {
        super.start()
        isStarted = true
    }

    override fun stop() {
        super.stop()
        isStarted = false
    }

}