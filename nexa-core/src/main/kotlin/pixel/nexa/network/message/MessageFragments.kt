package pixel.nexa.network.message

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.parser.Parser
import org.jsoup.safety.Safelist
import pixel.nexa.core.resource.asset.AbstractLanguage
import pixel.nexa.core.resource.asset.PageView
import pixel.nexa.core.resource.asset.PageViewContextBuilder
import pixel.nexa.core.util.BrowserUtils
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

interface ImageFragment : FileFragment, IDocumentSupport {

    override fun getFileName(language: AbstractLanguage) = "image.png"

    @OptIn(ExperimentalEncodingApi::class)
    override fun asNode(language: AbstractLanguage): Element = Element("img")
        .attr("alt", getFileName(language))
        .attr(
            "src",
            "data:image/${
                getFileName(language).split(".").last()
            };base64,${Base64.encode(inputStream(language).readAllBytes())}"
        )

}

class UnknownFragment(private val data: Any?) : TextFragment {
    override fun asText(language: AbstractLanguage) = data.toString()
}

object MessageFragments {

    /**
     * 直接解析文本
     */
    fun literal(literalString: String) = object : TextFragment {
        override fun asText(language: AbstractLanguage) = asNode(language).also {
            it.select("nexa-text").toList().forEach { node ->
                node.after(TextNode(node.attr("text")))
                node.remove()
            }
            it.select("nexa-html").toList().forEach { node ->
                node.after(Jsoup.parse(node.attr("html"), "", Parser.xmlParser()).text())
                node.remove()
            }
        }.html().let {
            val outputSettings = Document.OutputSettings().prettyPrint(false)
            Jsoup.clean(it, "", Safelist.none(), outputSettings)
        }

        override fun asNode(language: AbstractLanguage): Element {
            val parsed = Parser.parseXmlFragment(literalString, "")
            return if (parsed.size == 1 && parsed.firstOrNull() is Element) parsed.first() as Element
            else Element("div").appendChildren(Parser.parseXmlFragment(literalString, ""))
        }

    }

    /**
     * 普通文本
     */
    fun text(message: String) = object : TextFragment {
        override fun asText(language: AbstractLanguage) = message
        override fun asNode(language: AbstractLanguage) = TextNode(message)
    }

    /**
     * 页面浏览
     */
    fun pageView(pageView: PageView, block: PageViewContextBuilder.() -> Unit) = object : ImageFragment {
        private val byteArray: ByteArray by lazy {
            BrowserUtils.screenshot(pageView.render(block))
        }

        override fun inputStream(language: AbstractLanguage): InputStream {
            return ByteArrayInputStream(byteArray)
        }
    }

    /**
     * 文件
     */
    fun file(file: File) = object : FileFragment {
        override fun inputStream(language: AbstractLanguage) = file.inputStream()
        override fun getFileName(language: AbstractLanguage) = file.name
    }

    /**
     * 文件
     */
    fun file(inputStream: InputStream, name: String = "file") = object : FileFragment {
        override fun inputStream(language: AbstractLanguage) = inputStream
        override fun getFileName(language: AbstractLanguage) = name
    }

    /**
     * 文档
     */
    fun document(block: Document.() -> Unit) = object : ImageFragment {
        private var byteArray: ByteArray? = null
        override fun inputStream(language: AbstractLanguage): InputStream {
            return if (byteArray != null) ByteArrayInputStream(byteArray)
            else {
                byteArray = BrowserUtils.screenshot(Document(BrowserUtils.defaultURI.toString()).also(block))
                inputStream(language)
            }
        }
    }

    /**
     * 未知
     */
    fun unknown(data: Any?) = UnknownFragment(data)

    /**
     * 可翻译的
     */
    fun translatable(key: String, vararg args: Any?, fallback: () -> TextFragment = { literal(key) }) =
        TranslatableFragment(key, args, fallback)

    /**
     * 多个片段
     */
    fun multiple(vararg fragments: TextFragment) = MultiplyTextFragment(fragments)
    fun multiple(fragments: Iterable<TextFragment>) = MultiplyTextFragment(fragments.toList().toTypedArray())

}

class TranslatableFragment(val key: String, val args: Array<out Any?>, val fallback: () -> TextFragment) :
    TextFragment {

    @Suppress("UNUSED_PARAMETER")
    fun mapArguments(it: Any?, language: AbstractLanguage) = it
    fun mapTextArguments(it: Any?, language: AbstractLanguage) = when (it) {
        is TextFragment -> it.asText(language)
        else -> mapArguments(it, language)
    }

    fun mapNodeArguments(it: Any?, language: AbstractLanguage) = when (it) {
        is IDocumentSupport -> it.asNode(language)
        else -> mapArguments(it, language)
    }

    override fun asText(language: AbstractLanguage) =
        language.format(
            key,
            *args.map { mapTextArguments(it, language) }.toTypedArray(),
            fallback = { fallback().asText(language) })

    override fun asNode(language: AbstractLanguage): Node {
        var state = false
        val literal = MessageFragments.literal(
            language.format(
                key,
                *args.map { mapNodeArguments(it, language) }.toTypedArray(),
                fallback = { state = true; MessageFragments.literal(key).asText(language) })
        ).asNode(language)
        return if (state) fallback().asNode(language)
        else literal
    }

}

class MultiplyTextFragment(private val fragments: Array<out TextFragment>) : TextFragment {
    override fun asText(language: AbstractLanguage) = fragments.joinToString("") { it.asText(language) }
    override fun asNode(language: AbstractLanguage): Element =
        Element("div").appendChildren(fragments.map { it.asNode(language) })
}
