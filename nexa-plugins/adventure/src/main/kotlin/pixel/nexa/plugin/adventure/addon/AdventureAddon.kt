package pixel.nexa.plugin.adventure.addon

import pixel.nexa.core.registry.NexaRegistry
import pixel.nexa.plugin.adventure.entity.item.Item

interface AdventureAddon {

    fun registerItems(registry: NexaRegistry<Item>) {}

}