package pixel.nexa.core.registry

import pixel.auxframework.component.factory.getComponent
import pixel.auxframework.core.registry.IRegistry
import pixel.auxframework.core.registry.Registry
import pixel.auxframework.core.registry.ResourceKey
import pixel.nexa.core.platform.NexaContext
import pixel.nexa.core.platform.getListenersOfType

class NexaRegistry<T>(private val context: NexaContext, registryKey: ResourceKey<IRegistry<T>>) : Registry<T>(registryKey) {

    private val deferredRegistry by lazy {
        context.getAuxContext().componentFactory().getComponent<DeferredRegistry>()
    }

    fun getContext() = context

    override fun freeze() {
        context.getListenersOfType<BeforeRegistryFrozen>().forEach { it.beforeRegistryFrozen(this) }
        deferredRegistry.instances
            .filter { it.getRegistry().getRegistryKey() == getRegistryKey() }
            .forEach(DeferredRegister<*>::execute)
        super.freeze()
        context.getListenersOfType<AfterRegistryFrozen>().forEach { it.afterRegistryFrozen(this) }
    }

    override fun unfreeze() {
        context.getListenersOfType<BeforeRegistryUnfrozen>().forEach { it.beforeRegistryUnfrozen(this) }
        super.unfreeze()
        deferredRegistry.instances
            .filter { it.getRegistry().getRegistryKey() == getRegistryKey() }
            .forEach(DeferredRegister<*>::execute)
        context.getListenersOfType<AfterRegistryUnfrozen>().forEach { it.afterRegistryUnfrozen(this) }
    }

}

fun <T> NexaContext.createRegistry(registryKey: ResourceKey<IRegistry<T>>) = NexaRegistry(this, registryKey)
