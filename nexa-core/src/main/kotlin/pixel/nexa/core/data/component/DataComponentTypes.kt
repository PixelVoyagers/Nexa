package pixel.nexa.core.data.component

import pixel.auxframework.core.registry.Identifier
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.NexaCore
import pixel.nexa.core.data.tag.*
import pixel.nexa.core.resource.RootLanguage
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.TextFragment
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass

class ListDataType<T, E : ITag<*>>(private val type: IDataComponentType<T, E>) : IDataComponentType<List<T>, ListTag> {

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(tag: ListTag): List<T> = tag.map { type.deserialize(it as E) }.toList()

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

class LongDataType : IDataComponentType<Long, LongTag> {

    override fun deserialize(tag: LongTag) = tag.read()
    override fun serialize(element: Long) = LongTag(element)

}

class DoubleDataType : IDataComponentType<Double, DoubleTag> {

    override fun deserialize(tag: DoubleTag) = tag.read()
    override fun serialize(element: Double) = DoubleTag(element)

}

class FloatDataType : IDataComponentType<Float, FloatTag> {

    override fun deserialize(tag: FloatTag) = tag.read()
    override fun serialize(element: Float) = FloatTag(element)

}

class BigIntDataType : IDataComponentType<BigInteger, BigIntTag> {

    override fun deserialize(tag: BigIntTag) = tag.read()
    override fun serialize(element: BigInteger) = BigIntTag(element)

}


class BigDecimalDataType : IDataComponentType<BigDecimal, BigDecimalTag> {

    override fun deserialize(tag: BigDecimalTag) = tag.read()
    override fun serialize(element: BigDecimal) = BigDecimalTag(element)

}

class StringDataType : IDataComponentType<String, StringTag> {

    override fun deserialize(tag: StringTag) = tag.read()
    override fun serialize(element: String) = StringTag(element)

}

class BooleanDataType : IDataComponentType<Boolean, BooleanTag> {

    override fun deserialize(tag: BooleanTag) = tag.read()
    override fun serialize(element: Boolean) = if (element) BooleanTag.TRUE else BooleanTag.FALSE

}


class EnumDataType<T : Enum<T>>(private val enumClass: KClass<T>) : IDataComponentType<T, StringTag> {

    override fun deserialize(tag: StringTag): T = enumClass.java.enumConstants.first { it.name == tag.read() }
    override fun serialize(element: T) = StringTag(element.name)

}


class IdentifierDataType(private val defaultNamespace: String = NexaCore.DEFAULT_NAMESPACE) :
    IDataComponentType<Identifier, StringTag> {

    override fun deserialize(tag: StringTag) = identifierOf(tag.read(), defaultNamespace)
    override fun serialize(element: Identifier) = StringTag(element.toString())

}

class PairDataType<F, S>(
    private val first: IDataComponentType<F, ITag<*>>,
    private val second: IDataComponentType<S, ITag<*>>
) : IDataComponentType<Pair<F, S>, CompoundTag> {

    override fun deserialize(tag: CompoundTag) =
        first.deserialize(tag["first"] as ITag<*>) to second.deserialize(tag["second"] as ITag<*>)

    override fun serialize(element: Pair<@UnsafeVariance F, @UnsafeVariance S>): CompoundTag {
        val tag = CompoundTag()
        tag["first"] = first.serialize(element.first)
        tag["second"] = second.serialize(element.second)
        return tag
    }

}

class CompoundTagDataType : IDataComponentType<CompoundTag, CompoundTag> {

    override fun deserialize(tag: CompoundTag) = tag
    override fun serialize(element: CompoundTag) = element

}
