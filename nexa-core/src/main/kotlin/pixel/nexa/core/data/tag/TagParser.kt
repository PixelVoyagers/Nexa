package pixel.nexa.core.data.tag

import pixel.aurora.compiler.parser.*
import pixel.aurora.compiler.parser.util.ListParser
import pixel.aurora.compiler.parser.util.RawIdentifierParser
import pixel.aurora.compiler.tokenizer.NumericToken
import pixel.aurora.compiler.tokenizer.StringToken
import pixel.aurora.compiler.tokenizer.TokenType

class TagParser : Parser<ITag<*>>() {

    override fun parse() = include(CompoundTagParser() or ListTagParser() or BooleanTagParser() or StringTagParser() or NumberTagParser())

}

class ListTagParser : Parser<ListTag>() {

    override fun parse() = ListTag(list = include(ListParser(TagParser(), "[", "]", ",")).toMutableList())

}

class BooleanTagParser : Parser<BooleanTag>() {

    override fun parse(): BooleanTag {
        val boolean = buffer.get().expect(TokenType.BOOLEAN)
        return if (boolean.getRaw() == "true") BooleanTag.TRUE
        else BooleanTag.FALSE
    }

}

class StringTagParser : Parser<StringTag>() {

    override fun parse() = StringTag((buffer.get().expect(TokenType.STRING) as StringToken).getString())

}

class NumberTagParser : Parser<NumberTag<*>>() {

    override fun parse(): NumberTag<*> {
        val number = (buffer.get().expect(TokenType.NUMERIC) as NumericToken).getNumber()
        val typePart = include(RawIdentifierParser().optional()).getOrNull()
        val numberTag = NumberTag(number)
        return when (typePart?.lowercase()) {
            "b" -> ByteTag(numberTag.byteValue())
            "i" -> IntTag(numberTag.intValue())
            "l" -> LongTag(numberTag.longValue())
            "s" -> ShortTag(numberTag.shortValue())
            "f" -> FloatTag(numberTag.floatValue())
            "d" -> DoubleTag(numberTag.doubleValue())
            "bi" -> BigIntTag(numberTag.bigIntValue())
            "bd" -> BigDecimalTag(numberTag.bigDecimalValue())
            else -> numberTag
        }
    }

}

class CompoundTagParser : Parser<CompoundTag>() {

    override fun parse(): CompoundTag {
        val items = include(ListParser(pair(), "{", "}", ","))
        return compoundTagOf(items.toMap())
    }

    fun pair() = parser {
        val name = buffer.get().expect(TokenType.STRING) as StringToken
        buffer.get().expectPunctuation(':')
        val value = include(TagParser())
        name.getString() to value
    }

}