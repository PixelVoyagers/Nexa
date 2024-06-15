package pixel.nexa.plugin.adventure.entity.item

import pixel.auxframework.core.registry.Identifier
import pixel.auxframework.core.registry.RegistryAware
import pixel.nexa.core.data.component.*
import pixel.nexa.core.data.tag.CompoundTag
import pixel.nexa.core.data.tag.ITag
import pixel.nexa.core.data.tag.compoundTagOf
import pixel.nexa.core.registry.NexaRegistry
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.TextFragment
import pixel.nexa.plugin.adventure.AdventurePlugin

object ItemDataComponentTypes {

    object CustomDataField : IDataComponentType<CompoundTag, CompoundTag> {

        override fun deserialize(tag: CompoundTag) = tag
        override fun serialize(element: CompoundTag) = element

    }

    val CUSTOM_DATA = AdventurePlugin.id("custom_data") to CustomDataField
    val CUSTOM_NAME = AdventurePlugin.id("custom_name") to TextFragmentDataType()
    val CUSTOM_TOOLTIP = AdventurePlugin.id("custom_tooltip") to ListDataType(TextFragmentDataType())

}

open class Item(private val properties: Properties = Properties()) : RegistryAware<NexaRegistry<Item>> {

    class Properties {

        private val map: DataComponentMap = DataComponentMap().apply {
            schema.putAll(SCHEMA)
            load(CompoundTag())
        }

        companion object {

            val FIELD_RARITY = AdventurePlugin.id("rarity") to IntDataType()
            val FIELD_SHOW_DEFAULT_TOOLTIP = AdventurePlugin.id("show_default_tooltip") to BooleanDataType()

            val SCHEMA = mutableMapOf<Identifier, IDataComponentType<*, *>>(
                FIELD_RARITY, FIELD_SHOW_DEFAULT_TOOLTIP
            )

            fun copy(item: Item) = item.properties.copy()

        }

        fun <E, T : ITag<E>> component(type: Pair<Identifier, IDataComponentType<E, T>>) = map.getTyped(type.second)?.getOrNull()

        fun <E, T : ITag<E>> component(type: IDataComponentType<E, T>) = map.getTyped(type)?.getOrNull()
        fun <E, T : ITag<E>> component(name: Identifier) = map.get<E, T>(name)?.get()?.getOrNull()

        fun <E, T : ITag<E>> component(type: IDataComponentType<E, T>, value: E) = apply {
            map.put(type, value)
        }

        fun <E, T : ITag<E>> component(type: Pair<Identifier, IDataComponentType<E, T>>, value: E) = component(type.second, value)

        fun <E, T : ITag<E>> component(name: Identifier, value: E) = apply {
            map.put<E, T>(name, value)
        }

        fun rarity(level: Int) = component(FIELD_RARITY, level)
        fun rarity() = component(FIELD_RARITY) ?: 0

        fun showDefaultTooltip(show: Boolean) = component(FIELD_SHOW_DEFAULT_TOOLTIP, show)
        fun showDefaultTooltip() = component(FIELD_SHOW_DEFAULT_TOOLTIP) ?: false

        fun copy() = Properties().also {
            it.map.load(compoundTagOf(map.read().copy().read()))
        }

    }

    private lateinit var registry: NexaRegistry<Item>
    fun getRegistry() = registry

    override fun setRegistry(registry: NexaRegistry<Item>) {
        this.registry = registry
    }

    private val dataComponentSchema = mutableMapOf<Identifier, IDataComponentType<*, *>>(
        ItemDataComponentTypes.CUSTOM_DATA,
        ItemDataComponentTypes.CUSTOM_NAME
    )

    fun getDataComponentSchema() = dataComponentSchema

    open fun stackTo(count: Long = 1) = ItemStack(this, count)
    open fun getName(itemStack: ItemStack) = MessageFragments.translatable(getRegistry().get(this)!!.format { namespace, path -> "item.$namespace.$path.name" })


    open fun appendTooltip(itemStack: ItemStack, components: MutableList<TextFragment>) {}

}

