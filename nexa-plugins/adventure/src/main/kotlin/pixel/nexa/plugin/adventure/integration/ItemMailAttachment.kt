package pixel.nexa.plugin.adventure.integration

import pixel.auxframework.component.annotation.Component
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.data.tag.CompoundTag
import pixel.nexa.network.entity.user.User
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.plugin.adventure.entity.AdventureRegistries
import pixel.nexa.plugin.adventure.entity.item.ItemStack
import pixel.nexa.plugin.adventure.entity.item.ItemStackDataType
import pixel.nexa.plugin.adventure.handler.UserInventoryHandler
import pixel.nexa.plugin.profile.ProfilePlugin
import pixel.nexa.plugin.profile.handler.Mail
import pixel.nexa.plugin.profile.handler.MailHandler

class ItemMailAttachment(
    type: ItemMailAttachmentType,
    private val userInventoryHandler: UserInventoryHandler,
    val itemStack: ItemStack
) : Mail.Attachment(type) {

    override fun getDisplay() =
        MessageFragments.translatable("text.nexa-adventure.title.item", itemStack.getNameWithCount(true))

    override fun giveTo(user: User): Boolean {
        userInventoryHandler.editUserInventory(user) {
            give(itemStack)
        }
        return true
    }

}

@Component
class ItemMailAttachmentType(
    registries: AdventureRegistries,
    mailHandler: MailHandler,
    private val userInventoryHandler: UserInventoryHandler
) : Mail.AttachmentType<ItemMailAttachment> {

    fun create(itemStack: ItemStack) = ItemMailAttachment(this, userInventoryHandler, itemStack)

    init {
        mailHandler.attachmentTypes += identifierOf("${ProfilePlugin.PLUGIN_ID}:item") to this
    }

    private val itemStackDataType = ItemStackDataType(registries)

    override fun deserialize(tag: CompoundTag): ItemMailAttachment {
        val itemStack = itemStackDataType.deserialize(tag.getCompound("itemStack")!!)
        return ItemMailAttachment(this, userInventoryHandler, itemStack)
    }

    override fun serialize(element: ItemMailAttachment): CompoundTag {
        val tag = CompoundTag()
        tag.putCompound("itemStack", itemStackDataType.serialize(element.itemStack))
        return tag
    }

}