package pixel.nexa.plugin.adventure.entity.fluid

import pixel.auxframework.core.registry.Identifier
import pixel.auxframework.core.registry.RegistryAware
import pixel.nexa.core.data.component.*
import pixel.nexa.core.data.tag.CompoundTag
import pixel.nexa.core.data.tag.compoundTagOf
import pixel.nexa.core.registry.NexaRegistry
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.TextFragment
import pixel.nexa.plugin.adventure.AdventurePlugin

object FluidDataComponentTypes {

    val CUSTOM_DATA = AdventurePlugin.id("custom_data") to CompoundTagDataType()
    val CUSTOM_NAME = AdventurePlugin.id("custom_name") to TextFragmentDataType()
    val CUSTOM_TOOLTIP = AdventurePlugin.id("custom_tooltip") to ListDataType(TextFragmentDataType())

}

open class Fluid(private val properties: Properties = Properties()) : RegistryAware<NexaRegistry<Fluid>>, FluidLike,
    FluidStackLike {

    class Properties : ChainedDataComponentMap() {

        private val map: DataComponentMap = DataComponentMap().apply {
            schema.putAll(SCHEMA)
            load(CompoundTag())
        }

        override fun getMap() = map

        companion object {

            val FIELD_RARITY = AdventurePlugin.id("rarity") to IntDataType()
            val FIELD_SHOW_DEFAULT_TOOLTIP = AdventurePlugin.id("show_default_tooltip") to BooleanDataType()
            val FIELD_COLOR = AdventurePlugin.id("color") to IntDataType()

            val SCHEMA = mutableMapOf<Identifier, IDataComponentType<*, *>>(
                FIELD_RARITY, FIELD_SHOW_DEFAULT_TOOLTIP, FIELD_COLOR
            )

            fun copy(fluid: Fluid) = fluid.properties.copy()

        }

        fun rarity(level: Int) = apply {
            component(FIELD_RARITY, level)
        }

        fun rarity() = component(FIELD_RARITY) ?: 0

        fun showDefaultTooltip(show: Boolean) = apply {
            component(FIELD_SHOW_DEFAULT_TOOLTIP, show)
        }

        fun showDefaultTooltip() = component(FIELD_SHOW_DEFAULT_TOOLTIP) ?: false

        fun color() = component(FIELD_COLOR)
        fun color(color: Int) = apply {
            component(FIELD_COLOR, color)
        }

        fun copy() = Properties().also {
            it.map.load(compoundTagOf(map.read().copy().read()))
        }

    }

    open fun stackTo(count: Long = 1) = FluidStack(this, count)
    open fun getName(fluidStack: FluidStack) = MessageFragments.translatable(
        getRegistry().get(this)!!.format { namespace, path -> "fluid.$namespace.$path.name" })

    open fun getDataComponentSchema() = dataComponentSchema
    open fun appendTooltip(fluidStack: FluidStack, components: MutableList<TextFragment>) {}

    override fun asFluid() = this

    override fun asFluidStack() = stackTo()

    private lateinit var registry: NexaRegistry<Fluid>
    fun getRegistry() = registry
    override fun setRegistry(registry: NexaRegistry<Fluid>) {
        this.registry = registry
    }

    private val dataComponentSchema = mutableMapOf<Identifier, IDataComponentType<*, *>>(
        FluidDataComponentTypes.CUSTOM_DATA,
        FluidDataComponentTypes.CUSTOM_NAME
    )

}