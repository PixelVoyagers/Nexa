package pixel.nexa.plugin.profile.handler

import com.google.common.collect.HashBiMap
import pixel.auxframework.component.annotation.Component
import pixel.auxframework.core.registry.Identifier
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.NexaCore
import pixel.nexa.core.data.component.IDataComponentType
import pixel.nexa.core.data.component.ListDataType
import pixel.nexa.core.data.tag.CompoundTag
import pixel.nexa.core.data.tag.ListTag
import pixel.nexa.core.resource.asset.RootLanguage
import pixel.nexa.network.entity.user.User
import pixel.nexa.network.entity.user.UserDataSchema
import pixel.nexa.network.entity.user.editDataComponents
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.TextFragment
import pixel.nexa.plugin.profile.ProfilePlugin

class Mail(
    var title: TextFragment,
    var content: TextFragment,
    val attachments: MutableList<Attachment> = mutableListOf(),
    var isReceived: Boolean = false
) {

    var id: Long? = null

    abstract class Attachment(private val type: AttachmentType<*>) {

        open fun getType() = type

        abstract fun giveTo(user: User): Boolean
        abstract fun getDisplay(): TextFragment

    }

    interface AttachmentType<T : Attachment> : IDataComponentType<T, CompoundTag>

}

class MailDataType(private val handler: MailHandler) : IDataComponentType<Mail, CompoundTag> {

    fun deserializeAttachment(tag: CompoundTag): Mail.Attachment {
        val attachmentType = identifierOf(tag.getString("type")!!, NexaCore.DEFAULT_NAMESPACE)
        val data = tag.getCompound("data")!!
        return handler.attachmentTypes[attachmentType]!!.deserialize(data)
    }

    @Suppress("UNCHECKED_CAST")
    fun serializeAttachment(attachment: Mail.Attachment): CompoundTag {
        val attachmentTag = CompoundTag()
        attachmentTag.putString("type", handler.attachmentTypes.inverse()[attachment.getType()].toString())
        attachmentTag.putCompound(
            "data",
            (attachment.getType() as Mail.AttachmentType<Mail.Attachment>).serialize(attachment)
        )
        return attachmentTag
    }

    override fun deserialize(tag: CompoundTag): Mail {
        val title = MessageFragments.literal(tag.getString("title")!!)
        val content = MessageFragments.literal(tag.getString("content")!!)
        val id = tag.getLong("id")!!
        val received = tag.getBoolean("received") ?: false
        val attachments = tag.getList("attachments")!!.filterIsInstance<CompoundTag>().map(::deserializeAttachment)
        return Mail(title, content, attachments.toMutableList(), isReceived = received).apply {
            this.id = id
        }
    }

    override fun serialize(element: Mail): CompoundTag {
        val tag = CompoundTag()
        tag.putString("title", element.title.asNode(RootLanguage).toString())
        tag.putString("content", element.content.asNode(RootLanguage).toString())
        tag.putNumber("id", element.id!!)
        tag.putBoolean("received", element.isReceived)
        tag.putList(
            "attachments",
            ListTag().apply {
                for (attachment in element.attachments) {
                    add(serializeAttachment(attachment))
                }
            }
        )
        return tag
    }

}

@Component
class MailHandler(userDataSchema: UserDataSchema) {

    val attachmentTypes: HashBiMap<Identifier, Mail.AttachmentType<out Mail.Attachment>> =
        HashBiMap.create<Identifier, Mail.AttachmentType<out Mail.Attachment>>()
    val mailDataType = MailDataType(this)
    val userMailboxField = identifierOf("${ProfilePlugin.PLUGIN_ID}:mailbox") to ListDataType(mailDataType)

    init {
        userDataSchema.add(userMailboxField)
    }

    fun getUserMailbox(user: User): List<Mail> =
        user.getDataComponents().getTyped(userMailboxField.second)?.getOrNull() ?: emptyList()


    fun setUserMailbox(user: User, mails: List<Mail>) = user.editDataComponents {
        put(userMailboxField.second, mails)
    }

    fun addUserMail(user: User, vararg mail: Mail) {
        val mailbox = getUserMailbox(user).toMutableList()
        var currentId = (mailbox.mapNotNull { it.id }.maxOrNull() ?: 0) + 1
        for (one in mail) {
            one.id = currentId
            mailbox += one
            currentId++
        }
        setUserMailbox(user, mailbox)
    }

}
