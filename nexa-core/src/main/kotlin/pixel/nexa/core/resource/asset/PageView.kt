package pixel.nexa.core.resource.asset

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
import pixel.nexa.core.resource.NexaResource
import pixel.nexa.core.resource.NexaResourceLike
import java.util.*

@Service
class PageViewEngine(private val componentFactory: ComponentFactory) :
    AfterContextRefreshed {

    val engine = TemplateEngine()

    override fun afterContextRefreshed() {
        componentFactory.getComponents<IDialect>().forEach(engine::addDialect)
    }

    @Autowired
    private lateinit var assetsMap: AssetsMap

    fun getPage(identifier: Identifier) = memorize(identifier) {
        PageView(
            this,
            assetsMap[Identifier(identifier.getNamespace(), "pages/${identifier.getPath()}")]
        )
    }

}

open class PageView(
    private val pageViewEngine: PageViewEngine,
    val nexaResource: NexaResource
) : NexaResourceLike {

    override fun asResource() = nexaResource

    fun render(block: PageViewContextBuilder.() -> Unit): String {
        val builder = PageViewContextBuilder()
        return pageViewEngine.engine.process(
            nexaResource.contentAsString(),
            Context(Locale.ROOT, builder.also(block))
        )
    }

}

open class PageViewContextBuilder : MutableMap<String, Any?> by mutableMapOf() {

    operator fun String.invoke(value: Any?) {
        put(this@invoke, value)
    }

}
