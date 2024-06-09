package pixel.nexa.core.platform.adapter

import pixel.auxframework.component.factory.getComponent
import pixel.auxframework.util.FunctionUtils.memorize
import pixel.nexa.core.component.NexaContextAware
import pixel.nexa.core.platform.NexaContext
import pixel.nexa.core.service.NexaConfig
import java.util.*

abstract class AbstractNexaAdapter<T : NexaBot<T>, C : AbstractNexaAdapter.Companion.Config> : NexaAdapter<T, C>,
    NexaContextAware {

    companion object {

        abstract class Config {

            var enabled: Boolean = true
            var bots: Set<Map<String, Any>> = mutableSetOf()

        }

    }

    private lateinit var nexaContext: NexaContext
    private val bots: Set<T> = mutableSetOf()

    @Suppress("UNCHECKED_CAST")
    override fun getConfig(): C = memorize(this) {
        nexaContext.getAuxContext().componentFactory().getComponent<NexaConfig>().getAdapterConfig(this) as C
    }

    override fun isEnabled(): Boolean = getConfig().enabled

    override fun getBots(): Set<T> = Collections.unmodifiableSet(bots)

    override fun setNexaContext(context: NexaContext) {
        nexaContext = context
    }

    override fun getContext() = nexaContext

}