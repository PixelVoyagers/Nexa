package pixel.nexa.application

import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import pixel.auxframework.application.ApplicationContext
import pixel.auxframework.application.AuxApplication
import pixel.auxframework.application.AuxApplicationBuilder
import pixel.auxframework.application.Banner.Companion.VERSION_COLOR_FORMAT
import pixel.auxframework.component.factory.BeforeContextRefresh
import pixel.auxframework.component.factory.ComponentDefinition
import pixel.auxframework.component.factory.getComponent
import pixel.auxframework.component.factory.getComponents
import pixel.auxframework.core.AuxVersion
import pixel.auxframework.logging.common.AnsiColor
import pixel.auxframework.logging.common.AnsiFormat
import pixel.auxframework.logging.common.AnsiStyle
import pixel.auxframework.plugin.loader.AuxPluginContainer
import pixel.auxframework.plugin.loader.AuxPluginLoaderConfig
import pixel.nexa.core.NexaCore
import pixel.nexa.core.NexaVersion
import pixel.nexa.core.component.AfterResourceLoaded
import pixel.nexa.core.component.NexaContextAware
import pixel.nexa.core.platform.NexaContext
import pixel.nexa.core.resource.AssetsMap
import pixel.nexa.core.resource.ResourceLoader
import java.io.PrintStream

open class NexaApplication(private val nexaApplicationBuilder: NexaApplicationBuilder) :
    AuxApplication(nexaApplicationBuilder.getAuxApplicationBuilder()), BeforeContextRefresh {

    object Banner : pixel.auxframework.application.Banner {

        val BANNER_COLOR_FORMAT = AnsiFormat
            .Builder()
            .foregroundColor(AnsiColor.BLUE)
            .style(AnsiStyle.BOLD)
            .build()

        val BANNER = """
            __ _  ____  _  _   __  
            (  ( \(  __)( \/ ) / _\ 
            /    / ) _)  )  ( /    \
            \_)__)(____)(_/\_)\_/\_/
        """.trimIndent().lines()

        override fun printBanner(context: ApplicationContext, stream: PrintStream) {
            stream.println(
                BANNER.joinToString("\n") {
                    BANNER_COLOR_FORMAT.format(it)
                }
            )
            val versions = listOf(
                "Nexa" to NexaVersion.current().toString(),
                "AuxFramework" to AuxVersion.current().toString(),
                "Kotlin" to KotlinVersion.CURRENT,
                "JVM" to System.getProperty("java.vm.version", "<null>")
            )
            stream.println(
                versions.filter { it.second != "<null>" }.joinToString(separator = " ") {
                    "${VERSION_COLOR_FORMAT.format(it.first)}(${it.second})"
                }
            )
        }

    }

    init {
        nexaApplicationBuilder.context?.also {
            if (it is ApplicationNexaContext) it.application = this
        }
    }

    override fun beforeContextRefresh() {
        val pluginLoaderConfig = context.componentFactory().getComponent<AuxPluginLoaderConfig>()
        pluginLoaderConfig.directories += context.componentFactory().getComponent<NexaCore>().getDirectory("plugins")
    }

    fun getNexaApplicationBuilder() = nexaApplicationBuilder
    fun getNexaContext() = nexaApplicationBuilder.context!!

    override fun run(vararg args: String) {
        context.componentFactory().defineComponent(ComponentDefinition(getNexaContext(), loaded = true))
        super.run(*args)
        val pluginContainer = context.componentFactory().getComponent<AuxPluginContainer>()
        log.info(pluginContainer.getAll().toString())
        context.componentFactory().getComponents<NexaContextAware>().forEach { it.setNexaContext(getNexaContext()) }
        loadResources(pluginContainer)
        getNexaContext().start()
    }

    fun loadResources(pluginContainer: AuxPluginContainer) {
        val assetsMap = context.componentFactory().getComponent<AssetsMap>()
        val resourceLoader = context.componentFactory().getComponent<ResourceLoader>()
        for (classLoader in context.classLoaders) {
            resourceLoader
                .loadAsResourceMap("classpath:/assets", PathMatchingResourcePatternResolver(classLoader))
                .also { resources ->
                    assetsMap.apply {
                        load(resources)
                    }
                }
        }
        for (plugin in pluginContainer.getAll()) {
            val classLoader = runCatching { plugin.getPluginClassLoader() }.getOrNull() ?: continue
            val file = plugin.getPluginFile() ?: continue
            resourceLoader
                .loadAsResourceMap("jar:file:/${file.toPath()}!/assets", PathMatchingResourcePatternResolver(classLoader))
                .also { resources ->
                    assetsMap.apply {
                        load(resources)
                    }
                }
        }
        context.componentFactory().getComponents<AfterResourceLoaded>().forEach { it.afterResourceLoaded(assetsMap) }
    }

    override fun close() {
        getNexaContext().stop()
        super.close()
    }


}

data class NexaApplicationBuilder(
    val context: NexaContext? = null,
    val applicationBuilder: AuxApplicationBuilder.() -> AuxApplicationBuilder = { this }
) {

    fun context() = context
    fun context(context: NexaContext, block: NexaContext.() -> Unit = {}) = copy(context = context.also(block))

    fun applicationBuilder(block: AuxApplicationBuilder.() -> AuxApplicationBuilder = { this }) = copy(
        applicationBuilder = {
            block(applicationBuilder(this))
        }
    )

    fun complete() = copy(context = context ?: ApplicationNexaContext(), applicationBuilder = applicationBuilder)
    fun build() = NexaApplication(this.complete())

    fun getAuxApplicationBuilder() = applicationBuilder
        .invoke(
            AuxApplicationBuilder()
                .copy(banner = NexaApplication.Banner)
                .context(NexaApplicationContext())
        )

}
