package pixel.nexa.plugin.adventure.entity.item

import pixel.auxframework.component.annotation.Autowired
import pixel.auxframework.component.annotation.Component
import pixel.nexa.core.data.component.DataComponentMap
import pixel.nexa.core.data.component.IDataComponentType
import pixel.nexa.core.data.tag.CompoundTag
import pixel.nexa.core.data.tag.compoundTagOf
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.TextFragment
import pixel.nexa.plugin.adventure.AdventurePlugin
import pixel.nexa.plugin.adventure.entity.AdventureRegistries

@Component
class ItemStackDataType(private val registry: AdventureRegistries) : IDataComponentType<ItemStack, CompoundTag> {

    @Autowired
    private lateinit var items: Items

    override fun deserialize(tag: CompoundTag): ItemStack {
        val count = tag.getLong("count") ?: 1
        val item = registry.items.get(tag.getString("item")?.let(AdventurePlugin::id) ?: items.airItem.getLocation())
            ?: items.airItem.get()
        val data = tag.getCompound("data") ?: CompoundTag()
        val stack = ItemStack(item, count)
        stack.getDataComponents().load(data)
        return stack
    }

    override fun serialize(element: ItemStack): CompoundTag {
        val tag = CompoundTag()
        tag.putString("item", registry.items.get(element.getItem())!!.toString())
        tag.putNumber("count", element.getCount())
        tag.putCompound("data", element.getDataComponents().read())
        return tag
    }

}

class ItemStack(
    private val item: Item,
    private var count: Long = 1,
    private val dataComponentMap: DataComponentMap = DataComponentMap()
) : ItemLike, ItemStackLike {

    fun getDataComponents() = dataComponentMap
    fun getItem() = item
    fun getCount() = count

    fun isEmpty() = count == 0L || item is AirItem

    override fun asItem() = getItem()
    override fun asItemStack() = this

    fun setCount(to: Long) = apply {
        count = to
    }

    init {
        dataComponentMap.schema.putAll(getItem().getDataComponentSchema())
    }

    fun getName(): TextFragment {
        return getDataComponents().getTyped(ItemDataComponentTypes.CUSTOM_NAME.second)?.getOrNull()
            ?: getItem().getName(this)
    }

    fun getTooltip(): MutableList<TextFragment> {
        val tooltip = mutableListOf<TextFragment>()
        if (Item.Properties.copy(getItem()).showDefaultTooltip())
            tooltip += MessageFragments.translatable(
                getItem().getRegistry().get(getItem())!!.format { namespace, path -> "item.$namespace.$path.tooltip" })
        tooltip += getDataComponents().getTyped(ItemDataComponentTypes.CUSTOM_TOOLTIP.second)?.getOrNull()
            ?: emptyList()
        getItem().appendTooltip(this, tooltip)
        return tooltip
    }

    fun getNameWithCount(showId: Boolean = false): TextFragment {
        return MessageFragments.multiple(
            getName(),
            *if (showId) arrayOf(
                MessageFragments.text(
                    " (${
                        getItem().getRegistry().get(getItem()).toString()
                    })"
                )
            ) else emptyArray<TextFragment>(),
            MessageFragments.text(" * "),
            MessageFragments.text(getCount().toString())
        )
    }

    fun copy() = ItemStack(item, count).also {
        it.dataComponentMap.load(compoundTagOf(dataComponentMap.read().copy().read()))
    }

}