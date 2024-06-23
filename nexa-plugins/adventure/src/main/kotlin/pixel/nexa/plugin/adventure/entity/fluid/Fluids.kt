package pixel.nexa.plugin.adventure.entity.fluid

import pixel.auxframework.component.annotation.Component
import pixel.nexa.core.NexaCore
import pixel.nexa.core.registry.DeferredRegister
import pixel.nexa.plugin.adventure.entity.AdventureRegistries

@Component
class Fluids(registries: AdventureRegistries) {

    private val deferredRegister = DeferredRegister(registries.fluids, NexaCore.DEFAULT_NAMESPACE)

    val emptyFluid = deferredRegister.register("empty") { EmptyFluid() }

}

class EmptyFluid : Fluid()

