package pixel.nexa.core.component

import pixel.nexa.core.resource.ResourceMap

interface AfterResourceLoaded {

    fun afterResourceLoaded(resourceMap: ResourceMap)

}