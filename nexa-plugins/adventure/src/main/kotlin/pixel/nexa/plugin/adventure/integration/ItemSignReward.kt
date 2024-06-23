package pixel.nexa.plugin.adventure.integration

import pixel.auxframework.component.annotation.Component
import pixel.auxframework.context.builtin.AfterContextRefreshed
import pixel.nexa.network.entity.user.User
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.plugin.adventure.entity.item.ItemStack
import pixel.nexa.plugin.adventure.entity.item.Items
import pixel.nexa.plugin.adventure.handler.UserInventoryHandler
import pixel.nexa.plugin.profile.handler.SignHandler

class ItemSignReward(private val itemStack: ItemStack, private val inventoryHandler: UserInventoryHandler) :
    SignHandler.Reward {

    fun copyItemStack() = itemStack.copy()

    override fun getDisplay() =
        MessageFragments.translatable("text.nexa-adventure.reward.item", itemStack.getNameWithCount())

    override fun giveTo(user: User) {
        inventoryHandler.editUserInventory(user) {
            give(copyItemStack())
        }
    }

}

@Component
class ItemSignRewards(private val inventoryHandler: UserInventoryHandler, private val items: Items) :
    SignHandler.RewardSupplier, AfterContextRefreshed {

    val rewards = mutableSetOf<Pair<ItemStack, LongRange>>()

    override fun getRewards(user: User) =
        rewards.map { ItemSignReward(it.first.copy().apply { setCount(it.second.random()) }, inventoryHandler) }.toSet()

    override fun afterContextRefreshed() {
        rewards += ItemStack(items.coinItem.get()) to 5L..18L
    }

}
