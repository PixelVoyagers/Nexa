package pixel.nexa.plugin.adventure.handler

import pixel.auxframework.component.annotation.Component
import pixel.nexa.core.data.component.DataComponentMap
import pixel.nexa.core.data.component.IDataComponentType
import pixel.nexa.core.data.tag.CompoundTag
import pixel.nexa.core.data.tag.ListTag
import pixel.nexa.network.entity.user.User
import pixel.nexa.network.entity.user.UserDataSchema
import pixel.nexa.network.entity.user.editDataComponents
import pixel.nexa.plugin.adventure.AdventurePlugin
import pixel.nexa.plugin.adventure.entity.fluid.FluidLike
import pixel.nexa.plugin.adventure.entity.fluid.FluidStack
import pixel.nexa.plugin.adventure.entity.fluid.FluidStackDataType
import pixel.nexa.plugin.adventure.entity.fluid.FluidStackLike
import pixel.nexa.plugin.adventure.entity.item.ItemLike
import pixel.nexa.plugin.adventure.entity.item.ItemStack
import pixel.nexa.plugin.adventure.entity.item.ItemStackDataType
import pixel.nexa.plugin.adventure.entity.item.ItemStackLike


open class Inventory {

    private val items = mutableListOf<ItemStack>()
    private val fluids = mutableListOf<FluidStack>()

    open fun check() = apply {
        items.removeIf(ItemStack::isEmpty)
        fluids.removeIf(FluidStack::isEmpty)
    }

    fun getItems() = items
    fun getFluids() = fluids

    fun give(stackLike: ItemStackLike) {
        val stack = stackLike.asItemStack()
        if (stack.isEmpty()) return
        for (item in items) {
            if (item.getItem() === stack.getItem() && item.getDataComponents() == stack.getDataComponents()) {
                item.setCount(item.getCount() + stack.getCount())
                return
            }
        }
        items.add(stack)
        check()
    }

    fun drop(itemLike: ItemLike, amount: Long? = null, data: DataComponentMap? = null) {
        val item = itemLike.asItem()
        var needRemove = amount
        if (amount == null) for (i in items) {
            if (i.getItem() !== item && !(data == null || i.getDataComponents() == data)) continue
            i.setCount(0)
        } else for (i in this.items) {
            if (i.getItem() !== item && (data == null || i.getDataComponents() == data)) continue
            if (i.getCount() >= needRemove!!) {
                i.setCount(i.getCount() - needRemove)
                return
            }
            needRemove -= i.getCount()
            i.setCount(0)
        }
        check()
    }

    fun count(itemLike: ItemLike, data: DataComponentMap? = null): Long {
        val item = itemLike.asItem()
        check()
        return items
            .filter { i -> i.getItem() === item && (data == null || i.getDataComponents() == data) }
            .map(ItemStack::getCount)
            .reduce { a, b -> a + b }
    }

    fun give(stackLike: FluidStackLike) {
        val stack = stackLike.asFluidStack()
        if (stack.isEmpty()) return
        for (fluid in fluids) {
            if (fluid.getFluid() === stack.getFluid() && fluid.getDataComponents() == stack.getDataComponents()) {
                fluid.setCount(fluid.getCount() + stack.getCount())
                return
            }
        }
        fluids.add(stack)
        check()
    }

    fun drop(fluidLike: FluidLike, amount: Long? = null, data: DataComponentMap? = null) {
        val fluid = fluidLike.asFluid()
        var needRemove = amount
        if (amount == null) for (i in fluids) {
            if (i.getFluid() !== fluid && !(data == null || i.getDataComponents() == data)) continue
            i.setCount(0)
        } else for (i in this.fluids) {
            if (i.getFluid() !== fluid && (data == null || i.getDataComponents() == data)) continue
            if (i.getCount() >= needRemove!!) {
                i.setCount(i.getCount() - needRemove)
                return
            }
            needRemove -= i.getCount()
            i.setCount(0)
        }
        check()
    }

    fun count(fluidLike: FluidLike, data: DataComponentMap? = null): Long {
        val item = fluidLike.asFluid()
        check()
        return fluids
            .filter { i -> i.getFluid() === item && (data == null || i.getDataComponents() == data) }
            .map(FluidStack::getCount)
            .reduce { a, b -> a + b }
    }

}

@Component
class UserInventoryDataType(
    private val fluidStackDataType: FluidStackDataType,
    private val itemStackDataType: ItemStackDataType,
    userDataSchema: UserDataSchema
) : IDataComponentType<Inventory, CompoundTag> {

    init {
        userDataSchema.add(AdventurePlugin.id("inventory") to this)
    }

    override fun serialize(element: Inventory) = CompoundTag().apply {
        putList("items", ListTag(list = element.getItems().map(itemStackDataType::serialize).toMutableList()))
        putList("fluids", ListTag(list = element.getFluids().map(fluidStackDataType::serialize).toMutableList()))
    }

    override fun deserialize(tag: CompoundTag) = Inventory().apply {
        getItems() += (tag.getList("items") ?: ListTag()).filterIsInstance<CompoundTag>()
            .map(itemStackDataType::deserialize)
        getFluids() += (tag.getList("fluids") ?: ListTag()).filterIsInstance<CompoundTag>()
            .map(fluidStackDataType::deserialize)
        check()
    }

}

@Component
class UserInventoryHandler(private val userInventoryDataType: UserInventoryDataType) {

    fun getUserInventory(user: User) = user.getDataComponents().getOrPut(userInventoryDataType) { Inventory() }
    fun setUserInventory(user: User, inventory: Inventory) = user.editDataComponents {
        put(userInventoryDataType, inventory)
    }

    fun editUserInventory(user: User, block: Inventory.() -> Unit) {
        val inventory = getUserInventory(user)
        inventory.block()
        setUserInventory(user, inventory)
    }

}
