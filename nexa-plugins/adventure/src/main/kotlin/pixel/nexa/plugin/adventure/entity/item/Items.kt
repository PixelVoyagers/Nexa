package pixel.nexa.plugin.adventure.entity.item

import pixel.auxframework.component.annotation.Component
import pixel.nexa.core.registry.DeferredRegister
import pixel.nexa.plugin.adventure.entity.AdventureRegistries

@Component
class Items(registries: AdventureRegistries) {

    private val deferredRegister = DeferredRegister(registries.items)

    val airItem = deferredRegister.register("air") { AirItem() }
    val coinItem = deferredRegister.register("coin") { Item(Item.Properties().rarity(1).showDefaultTooltip(true)) }

}

class AirItem : Item()
