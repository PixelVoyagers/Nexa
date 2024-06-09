package pixel.nexa.application

import kotlinx.coroutines.runBlocking
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import pixel.auxframework.application.ApplicationContext
import pixel.auxframework.application.AuxApplication
import pixel.auxframework.application.AuxApplicationBuilder
import pixel.auxframework.application.Banner.Companion.VERSION_COLOR_FORMAT
import pixel.auxframework.component.factory.ComponentDefinition
import pixel.auxframework.component.factory.getComponent
import pixel.auxframework.component.factory.getComponents
import pixel.auxframework.core.AuxVersion
import pixel.auxframework.logging.common.AnsiColor
import pixel.auxframework.logging.common.AnsiFormat
import pixel.auxframework.logging.common.AnsiStyle
import pixel.auxframework.plugin.loader.AuxPluginContainer
import pixel.auxframework.plugin.loader.AuxPluginLoader
import pixel.auxframework.plugin.loader.AuxPluginLoaderConfig
import pixel.nexa.core.NexaCore
import pixel.nexa.core.NexaVersion
import pixel.nexa.core.component.NexaContextAware
import pixel.nexa.core.platform.NexaContext
import pixel.nexa.core.resource.AssetsMap
import pixel.nexa.core.resource.ResourceLoader
import java.io.PrintStream

open class NexaApplication(private val nexaApplicationBuilder: NexaApplicationBuilder) :
    AuxApplication(nexaApplicationBuilder.getAuxApplicationBuilder()) {

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

    fun getNexaApplicationBuilder() = nexaApplicationBuilder
    fun getNexaContext() = nexaApplicationBuilder.context!!

    fun loadPlugins() = runBlocking {
        val pluginLoaderConfig = context.componentFactory().getComponent<AuxPluginLoaderConfig>()
        pluginLoaderConfig.directories += context.componentFactory().getComponent<NexaCore>().getDirectory("plugins")
        val pluginLoader = context.componentFactory().getComponent<AuxPluginLoader>()
        pluginLoader.initializePlugins(pluginLoader.scanPlugins())
        context.componentFactory().getComponent<AuxPluginContainer>()
    }

    override fun run(vararg args: String) {
        context.componentFactory().defineComponent(ComponentDefinition(getNexaContext(), loaded = true))
        super.run(*args)
        val pluginContainer = loadPlugins()
        context.componentFactory().getComponents<NexaContextAware>().forEach { it.setNexaContext(getNexaContext()) }
        val classLoaders = mutableSetOf(this::class.java.classLoader)
        for (plugin in pluginContainer.getAll()) runCatching {
            classLoaders += plugin.getPluginClassLoader()
        }
        for (classLoader in classLoaders) {
            context.componentFactory().getComponent<ResourceLoader>()
                .loadAsResourceMap("classpath:/assets", PathMatchingResourcePatternResolver(classLoader))
                .also { resources ->
                    context.componentFactory().getComponent<AssetsMap>().apply {
                        load(resources)
                    }
                }
        }
        getNexaContext().start()
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
