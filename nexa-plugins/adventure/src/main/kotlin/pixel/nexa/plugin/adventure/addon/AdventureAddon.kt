package pixel.nexa.plugin.adventure.addon

import pixel.nexa.core.registry.NexaRegistry
import pixel.nexa.plugin.adventure.entity.fluid.Fluid
import pixel.nexa.plugin.adventure.entity.item.Item

interface AdventureAddon {

    fun beforeRegister() {}
    fun postRegister() {}
    fun registerItems(registry: NexaRegistry<Item>) {}
    fun registerFluids(registry: NexaRegistry<Fluid>) {}

}