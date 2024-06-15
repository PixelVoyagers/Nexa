package pixel.nexa.core.resource

import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.dialect.IDialect
import pixel.auxframework.component.annotation.Autowired
import pixel.auxframework.component.annotation.Service
import pixel.auxframework.component.factory.ComponentFactory
import pixel.auxframework.component.factory.getComponents
import pixel.auxframework.context.builtin.AfterContextRefreshed
import pixel.auxframework.core.registry.Identifier
import pixel.auxframework.util.FunctionUtils.memorize
import java.util.*

@Service
class PageViewEngine(private val resourceLoader: ResourceLoader, private val componentFactory: ComponentFactory) : AfterContextRefreshed {

    val engine = TemplateEngine()

    override fun afterContextRefreshed() {
        componentFactory.getComponents<IDialect>().forEach(engine::addDialect)
    }

    @Autowired
    private lateinit var assetsMap: AssetsMap

    fun getPage(identifier: Identifier) = memorize(identifier) {
        PageView(this, assetsMap[Identifier(identifier.getNamespace(), "pages/${identifier.getPath()}")], resourceLoader)
    }

}

open class PageView(private val pageViewEngine: PageViewEngine, val nexaResource: NexaResource, val resourceLoader: ResourceLoader) {

    fun render(block: MutableMap<String, Any>.() -> Unit): String {
        return pageViewEngine.engine.process(
            nexaResource.contentAsString(),
            Context(Locale.ROOT, mutableMapOf<String, Any>().also(block))
        )
    }

}
