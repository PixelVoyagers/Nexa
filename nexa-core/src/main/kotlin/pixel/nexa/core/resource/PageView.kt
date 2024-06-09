package pixel.nexa.core.resource

import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.util.*

open class PageView(val nexaResource: NexaResource, val resourceLoader: ResourceLoader) {

    companion object {
        val ENGINE = TemplateEngine()
    }

    fun render(block: MutableMap<String, Any>.() -> Unit): String = ENGINE.process(nexaResource.contentAsString(), Context(Locale.ROOT, mutableMapOf<String, Any>().also(block)))

}