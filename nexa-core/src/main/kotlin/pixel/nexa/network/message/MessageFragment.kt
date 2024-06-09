package pixel.nexa.network.message

import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import pixel.nexa.core.resource.AbstractLanguage
import java.io.InputStream

/**
 * 消息片段
 */
interface MessageFragment

/**
 * 文档节点支持
 */
interface IDocumentSupport {
    fun asNode(language: AbstractLanguage): Node
}

/**
 * 文本片段
 */
interface TextFragment : MessageFragment, IDocumentSupport {
    fun asText(language: AbstractLanguage): String
    override fun asNode(language: AbstractLanguage): Node = TextNode(asText(language))
}

/**
 * 文件片段
 */
interface FileFragment : MessageFragment {

    fun inputStream(language: AbstractLanguage): InputStream
    fun getFileName(language: AbstractLanguage): String = "file"

}

