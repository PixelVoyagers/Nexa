package pixel.nexa.core.data.component

import pixel.aurora.compiler.AuroraCompiler
import pixel.aurora.compiler.parser.Parser
import pixel.aurora.compiler.parser.buffer
import pixel.aurora.compiler.parser.include
import pixel.aurora.compiler.parser.parser
import pixel.aurora.compiler.parser.util.ListParser
import pixel.aurora.compiler.tokenizer.StringToken
import pixel.aurora.compiler.tokenizer.TokenBuffer
import pixel.aurora.compiler.tokenizer.TokenType
import pixel.aurora.compiler.tokenizer.Tokenizer
import pixel.auxframework.core.registry.Identifier
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.data.tag.CompoundTag
import pixel.nexa.core.data.tag.TagParser
import java.nio.CharBuffer

class DataCompoundMapParser(val schema: Map<Identifier, IDataComponentType<*, *>>) : Parser<DataComponentMap>() {

    companion object {

        fun parseDataComponentMap(input: String, schema: Map<Identifier, IDataComponentType<*, *>>) = DataCompoundMapParser(schema).apply {
            setState(
                State(
                    uri = AuroraCompiler.BLANK_URI,
                    buffer = TokenBuffer(Tokenizer(CharBuffer.wrap(input), AuroraCompiler.BLANK_URI))
                )
            )
        }.parse()

    }

    override fun parse(): DataComponentMap {
        val map = DataComponentMap()
        map.schema += schema
        val entries = include(ListParser(pair(), "[", "]", ",")).toMap().mapKeys { it.key.toString() }
        val tag = CompoundTag()
        for (entry in entries) tag[entry.key] = entry.value
        map.load(tag)
        return map
    }

    fun pair() = parser {
        val name = buffer.get().expect(TokenType.STRING) as StringToken
        buffer.get().expectPunctuation('=')
        identifierOf(name.getString()) to include(TagParser())
    }

}
