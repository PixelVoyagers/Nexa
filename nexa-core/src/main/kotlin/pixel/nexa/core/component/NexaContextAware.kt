package pixel.nexa.core.component

import pixel.auxframework.component.factory.Aware
import pixel.nexa.core.platform.NexaContext

interface NexaContextAware : Aware {

    fun setNexaContext(context: NexaContext)

}