package pixel.nexa.core.registry

interface BeforeRegistryFrozen {

    fun beforeRegistryFrozen(registry: NexaRegistry<*>)

}

interface AfterRegistryFrozen {

    fun afterRegistryFrozen(registry: NexaRegistry<*>)

}

interface BeforeRegistryUnfrozen {

    fun beforeRegistryUnfrozen(registry: NexaRegistry<*>)

}

interface AfterRegistryUnfrozen {

    fun afterRegistryUnfrozen(registry: NexaRegistry<*>)

}
