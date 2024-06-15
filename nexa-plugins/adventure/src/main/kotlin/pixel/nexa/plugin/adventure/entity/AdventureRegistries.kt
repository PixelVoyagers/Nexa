package pixel.nexa.plugin.adventure.entity

import pixel.auxframework.component.annotation.Component
import pixel.auxframework.context.builtin.AfterContextRefreshed
import pixel.auxframework.core.registry.IRegistry
import pixel.auxframework.core.registry.ResourceKey
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.platform.NexaContext
import pixel.nexa.core.registry.createRegistry
import pixel.nexa.plugin.adventure.AdventurePlugin
import pixel.nexa.plugin.adventure.addon.AdventureAddonRepository
import pixel.nexa.plugin.adventure.entity.item.Item

@Component
class AdventureRegistries(nexaContext: NexaContext, private val addonRepository: AdventureAddonRepository) : AfterContextRefreshed {

    private fun <T> key(name: String) = ResourceKey<IRegistry<T>>(identifierOf("root", AdventurePlugin.PLUGIN_ID), identifierOf(name, AdventurePlugin.PLUGIN_ID))

    val items = nexaContext.createRegistry<Item>(key("item"))

    override fun afterContextRefreshed() {
        items.unfreeze()
        addonRepository.getAll().forEach { it.registerItems(items) }
        items.freeze()
    }

}