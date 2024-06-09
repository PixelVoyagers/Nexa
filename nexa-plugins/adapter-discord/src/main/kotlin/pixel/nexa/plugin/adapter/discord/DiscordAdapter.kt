package pixel.nexa.plugin.adapter.discord

import pixel.auxframework.component.annotation.Service
import pixel.auxframework.util.FunctionUtils.memorize
import pixel.nexa.core.annotation.Adapter
import pixel.nexa.core.platform.adapter.AbstractNexaAdapter

@Adapter
@Service
class DiscordAdapter : AbstractNexaAdapter<DiscordBot, DiscordAdapter.Config>() {

    class Config : Companion.Config()

    override fun getBots() = memorize {
        getConfig().bots.map {
            val bot = DiscordBot(this@DiscordAdapter, it)
            bot.setName(it["name"].toString())
            bot
        }.toMutableSet()
    }

    override fun getPlatform() = "discord"

}
