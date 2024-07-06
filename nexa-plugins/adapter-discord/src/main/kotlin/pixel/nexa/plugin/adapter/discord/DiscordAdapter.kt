package pixel.nexa.plugin.adapter.discord

import pixel.auxframework.component.annotation.Component
import pixel.auxframework.component.annotation.Service
import pixel.auxframework.util.FunctionUtils.memorize
import pixel.nexa.core.annotation.Adapter
import pixel.nexa.core.platform.adapter.AbstractNexaAdapter

@Adapter
@Service
@Component
class DiscordAdapter : AbstractNexaAdapter<DiscordBot, DiscordAdapter.Config>() {

    companion object {
        const val PLATFORM_NAME = "discord"
    }

    class Config : AbstractNexaAdapter.Companion.Config()

    override fun getBots() = memorize(this) {
        getConfig().bots.map {
            val bot = DiscordBot(this@DiscordAdapter, it)
            bot.setName(it["name"].toString())
            bot
        }.toMutableSet()
    }

    override fun getPlatform() = PLATFORM_NAME

}
