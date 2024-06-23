package pixel.nexa.plugin.adventure.entity.fluid

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
import pixel.nexa.plugin.adventure.entity.item.ItemDataComponentTypes
import pixel.nexa.plugin.adventure.entity.item.Items

@Component
class FluidStackDataType(private val registry: AdventureRegistries) : IDataComponentType<FluidStack, CompoundTag> {

    @Autowired
    private lateinit var items: Items

    @Autowired
    private lateinit var fluids: Fluids

    override fun deserialize(tag: CompoundTag): FluidStack {
        val count = tag.getLong("count") ?: 1
        val fluid =
            registry.fluids.get(tag.getString("fluid")?.let(AdventurePlugin::id) ?: fluids.emptyFluid.getLocation())
                ?: fluids.emptyFluid.get()
        val data = tag.getCompound("data") ?: CompoundTag()
        val stack = FluidStack(fluid, count)
        stack.getDataComponents().load(data)
        return stack
    }

    override fun serialize(element: FluidStack): CompoundTag {
        val tag = CompoundTag()
        tag.putString("fluid", registry.fluids.get(element.getFluid())!!.toString())
        tag.putNumber("count", element.getCount())
        tag.putCompound("data", element.getDataComponents().read())
        return tag
    }

}

class FluidStack(
    private val fluid: Fluid,
    private var count: Long = 1,
    private val dataComponentMap: DataComponentMap = DataComponentMap()
) : FluidLike, FluidStackLike {

    fun getDataComponents() = dataComponentMap
    fun getFluid() = fluid
    fun getCount() = count

    init {
        dataComponentMap.schema.putAll(getFluid().getDataComponentSchema())
    }

    override fun asFluid() = fluid
    override fun asFluidStack() = this

    fun isEmpty() = count == 0L || fluid is EmptyFluid


    fun setCount(to: Long) = apply {
        count = to
    }

    fun getName(): TextFragment {
        return getDataComponents().getTyped(ItemDataComponentTypes.CUSTOM_NAME.second)?.getOrNull()
            ?: getFluid().getName(this)
    }

    fun getTooltip(): MutableList<TextFragment> {
        val tooltip = mutableListOf<TextFragment>()
        if (Fluid.Properties.copy(getFluid()).showDefaultTooltip())
            tooltip += MessageFragments.translatable(
                getFluid().getRegistry().get(getFluid())!!
                    .format { namespace, path -> "fluid.$namespace.$path.tooltip" })
        tooltip += getDataComponents().getTyped(ItemDataComponentTypes.CUSTOM_TOOLTIP.second)?.getOrNull()
            ?: emptyList()
        getFluid().appendTooltip(this, tooltip)
        return tooltip
    }

    fun getNameWithCount(): TextFragment {
        return MessageFragments.multiple(
            getName(),
            MessageFragments.text(" * "),
            MessageFragments.text(getCount().toString())
        )
    }

    fun copy() = FluidStack(getFluid(), getCount()).also {
        it.dataComponentMap.load(compoundTagOf(dataComponentMap.read().copy().read()))
    }

}