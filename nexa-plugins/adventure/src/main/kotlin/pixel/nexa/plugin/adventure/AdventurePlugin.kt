package pixel.nexa.plugin.adventure

import pixel.auxframework.core.registry.identifierOf

object AdventurePlugin {

    fun id(path: String) = identifierOf(path, PLUGIN_ID)

    const val PLUGIN_ID = "nexa-adventure"

}