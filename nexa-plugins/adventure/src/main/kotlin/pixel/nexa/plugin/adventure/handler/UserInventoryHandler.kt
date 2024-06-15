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
import pixel.nexa.plugin.adventure.entity.item.Item
import pixel.nexa.plugin.adventure.entity.item.ItemStack
import pixel.nexa.plugin.adventure.entity.item.ItemStackDataType


open class Inventory {

    private val items = mutableListOf<ItemStack>()

    protected fun check() = apply {
        items.removeIf(ItemStack::isEmpty)
    }

    fun getItems() = items

    fun give(stack: ItemStack) {
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

    fun drop(item: Item, amount: Long? = null, data: DataComponentMap? = null) {
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

    fun count(item: Item, data: DataComponentMap? = null): Long {
        check()
        return items
            .filter { i -> i.getItem() === item && (data == null || i.getDataComponents() == data) }
            .map(ItemStack::getCount)
            .reduce { a, b -> a + b }
    }

}

@Component
class UserInventoryDataType(private val itemStackDataType: ItemStackDataType, userDataSchema: UserDataSchema) : IDataComponentType<Inventory, CompoundTag> {

    init {
        userDataSchema.add(AdventurePlugin.id("inventory") to this)
    }

    override fun serialize(element: Inventory) = CompoundTag().apply {
        putList(
            "items",
            element.getItems().map(itemStackDataType::serialize)
        )
    }

    override fun deserialize(tag: CompoundTag) = Inventory().apply {
        getItems() += (tag.getList("items") ?: ListTag()).filterIsInstance<CompoundTag>().map(itemStackDataType::deserialize)
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
