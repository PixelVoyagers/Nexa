package pixel.nexa.core.resource.asset

import com.google.common.collect.HashBiMap
import pixel.auxframework.component.annotation.Autowired
import pixel.auxframework.component.annotation.Component
import pixel.auxframework.component.annotation.Repository
import pixel.auxframework.util.ConfigUtils
import pixel.auxframework.util.useAuxConfig
import pixel.nexa.core.NexaCore
import pixel.nexa.core.component.AfterResourceLoaded
import pixel.nexa.core.platform.NexaContext
import pixel.nexa.core.resource.ResourceMap
import pixel.nexa.core.util.ResourceUtils
import java.util.*

/**
 * 国际化配置文件
 */
class LanguageConfig {
    var defaultLanguage: String = "zh_hans"
}

/**
 * 语言注册表
 */
@Repository
class Languages(private val context: NexaContext, core: NexaCore) {

    private val config: LanguageConfig = core.getDirectory("config/nexa/i18n/").useAuxConfig<LanguageConfig>("i18n.yml")

    private val languages = HashBiMap.create<String, AbstractLanguage>().apply {
        put("root", RootLanguage)
    }

    fun getEntries(): HashBiMap<String, AbstractLanguage> = languages
    fun getName(language: AbstractLanguage) = languages.inverse()[language]
    fun getRoot() = languages["root"]!!
    fun getLanguages(): HashBiMap<String, AbstractLanguage> = languages
    fun getLanguageOrPut(key: String, default: () -> AbstractLanguage): AbstractLanguage =
        languages.getOrPut(key, default)

    fun getDefault() = languages[config.defaultLanguage] ?: languages["root"]!!
    fun getLanguage(language: String, default: AbstractLanguage = getDefault()) = getLanguageOrNull(language) ?: default
    fun getLanguageOrNull(language: String) = languages[language]
}

/**
 * 语言抽象类
 */
abstract class AbstractLanguage : MutableMap<String, String> by mutableMapOf() {
    open fun getLocaleTag(platform: String): String = getOrDefault("language.locale.$platform")
    abstract fun getCompletionRate(compare: AbstractLanguage): Double
    open fun format(key: String, vararg values: Any?, fallback: () -> String = { key }): String {
        return getOrDefault(key, "null").let { if (it == "null") fallback() else it }.format(Locale.ROOT, *values)
    }

    open fun getOrDefault(key: String, defaultValue: String = key) = super.getOrDefault(key, defaultValue)
    open fun getOrNull(key: String) = this[key]
}

/**
 * 上下文语言
 */
open class ContextLanguage(private val context: NexaContext) : AbstractLanguage() {

    override fun getCompletionRate(compare: AbstractLanguage): Double {
        return compare.keys.filter { this.contains(it) }.size.toDouble() / compare.keys.size.toDouble()
    }

}

/**
 * 空语言
 */
object RootLanguage : AbstractLanguage() {
    override fun get(key: String) = key
    override fun getOrDefault(key: String, defaultValue: String) = defaultValue
    override fun put(key: String, value: String) = value
    override fun putAll(from: Map<out String, String>) {}
    override fun putIfAbsent(key: String, value: String) = value
    override fun getCompletionRate(compare: AbstractLanguage) = 0.0
    override fun getLocaleTag(platform: String) = "root"
}

@Component
class LanguageResourcesProcessor : AfterResourceLoaded {

    @Autowired
    private lateinit var assetsMap: AssetsMap

    @Autowired
    private lateinit var context: NexaContext

    @Autowired
    private lateinit var languages: Languages

    override fun afterResourceLoaded(resourceMap: ResourceMap) {
        for (resource in assetsMap.values()) {
            if (resource.getResourceType().getPath() == "languages") {
                val languageName = resource.getFileName(false)
                val name = resource.getFileName(true)
                val type = when {
                    name.endsWith(".yml") || name.endsWith(".yaml") -> ConfigUtils.ConfigTypes.YAML
                    name.endsWith(".json") -> ConfigUtils.ConfigTypes.JSON
                    name.endsWith(".xml") -> ConfigUtils.ConfigTypes.XML
                    name.endsWith(".toml") -> ConfigUtils.ConfigTypes.XML
                    name.endsWith(".csv") -> ConfigUtils.ConfigTypes.CSV
                    else -> continue
                }
                val map = ResourceUtils.flattenStringMap(ResourceUtils.resolveToMap(resource, type))
                val languageInstance = languages.getLanguageOrPut(languageName) {
                    ContextLanguage(context)
                }
                languageInstance.putAll(map)
            }
        }
    }

}
