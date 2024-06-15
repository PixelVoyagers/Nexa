package pixel.nexa.core.data.component

import pixel.nexa.core.data.tag.*
import pixel.nexa.core.resource.RootLanguage
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.TextFragment

class ListDataType<T>(private val type: IDataComponentType<T, *>) : IDataComponentType<List<T>, ListTag> {

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(tag: ListTag): List<T> = tag.map { (type as IDataComponentType<T, ITag<*>>).deserialize(it) }

    override fun serialize(element: List<T>): ListTag {
        val listTag = ListTag()
        for (item in element) {
            listTag.add(type.serialize(item))
        }
        return listTag
    }

}

class TextFragmentDataType : IDataComponentType<TextFragment, StringTag> {

    override fun deserialize(tag: StringTag) = MessageFragments.literal(tag.read())
    override fun serialize(element: TextFragment) = StringTag(element.asText(RootLanguage))

}

class IntDataType : IDataComponentType<Int, IntTag> {

    override fun deserialize(tag: IntTag) = tag.read()
    override fun serialize(element: Int) = IntTag(element)

}

class StringDataType : IDataComponentType<String, StringTag> {

    override fun deserialize(tag: StringTag) = tag.read()
    override fun serialize(element: String) = StringTag(element)

}

class BooleanDataType : IDataComponentType<Boolean, BooleanTag> {

    override fun deserialize(tag: BooleanTag) = tag.read()
    override fun serialize(element: Boolean) = if (element) BooleanTag.TRUE else BooleanTag.FALSE

}
