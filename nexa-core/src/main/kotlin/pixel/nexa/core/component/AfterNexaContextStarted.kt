package pixel.nexa.core.component

import pixel.nexa.core.platform.NexaContext

interface AfterNexaContextStarted {

    fun afterNexaContextStarted(context: NexaContext)

}