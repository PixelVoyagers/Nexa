package pixel.nexa.plugin.adventure.entity.item

import pixel.auxframework.component.annotation.Component
import pixel.nexa.core.NexaCore
import pixel.nexa.core.registry.DeferredRegister
import pixel.nexa.plugin.adventure.entity.AdventureRegistries

@Component
class Items(registries: AdventureRegistries) {

    private val deferredRegister = DeferredRegister(registries.items, NexaCore.DEFAULT_NAMESPACE)

    val airItem = deferredRegister.register("air") { AirItem() }
    val coinItem = deferredRegister.register("coin") { Item(Item.Properties().rarity(3).showDefaultTooltip(true)) }

}

class AirItem : Item()
