package pixel.nexa.core.util

import com.microsoft.playwright.Browser
import com.microsoft.playwright.ElementHandle
import com.microsoft.playwright.Playwright
import com.microsoft.playwright.options.ScreenshotType
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import java.net.URI

/**
 * 浏览器实用工具库
 */
object BrowserUtils {

    /**
     * 默认打开的地址
     */
    val defaultURI: URI = URI.create("about:blank")

    /**
     * Playwright实例
     */
    val playwright: Playwright = Playwright.create()

    /**
     * 浏览器实例
     */
    val browser: Browser = playwright.chromium().launch()

    /**
     * 截图
     */
    fun screenshot(html: String, type: ScreenshotType = ScreenshotType.PNG): ByteArray {
        val page = browser.newPage()
        page.navigate(defaultURI.toString())
        page.setContent(html)
        val viewport = page.viewportSize()
        val height =
            page.evaluate("window?.nexa?.viewport?.height ?? document.documentElement.scrollHeight")?.toString()
                ?.toInt() ?: viewport.height
        val width =
            page.evaluate("window?.nexa?.viewport?.width ?? document.documentElement.scrollWidth")?.toString()?.toInt()
                ?: viewport.width
        page.setViewportSize(width, height)
        val selector = page.evaluate("window?.nexa?.screenshot?.selector ?? 'html'")
        page.querySelector(selector.toString())
        val screenshot =
            page.querySelector(selector.toString()).screenshot(ElementHandle.ScreenshotOptions().setType(type))
        page.close()
        return screenshot
    }

    /**
     * 截图
     */
    fun screenshot(node: Node, type: ScreenshotType = ScreenshotType.PNG): ByteArray {
        return screenshot(node.toString(), type)
    }

    /**
     * body 元素
     */
    fun Document.body(block: Element.() -> Unit): Element = body().also(block)

    /**
     * head 元素
     */
    fun Document.head(block: Element.() -> Unit): Element = body().also(block)

    /**
     * 元素
     */
    fun Element.element(name: String, namespace: String? = null, block: Element.() -> Unit): Element {
        val element = if (namespace == null) appendElement(name)
        else appendElement(namespace)
        block(element)
        return element
    }

    /**
     * 元素类名
     */
    fun Element.classes(vararg className: String) = this.also {
        className.forEach(this::addClass)
    }

}
