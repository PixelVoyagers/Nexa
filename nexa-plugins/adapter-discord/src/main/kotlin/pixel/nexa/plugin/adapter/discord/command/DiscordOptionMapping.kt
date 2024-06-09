package pixel.nexa.plugin.adapter.discord.command

import pixel.nexa.network.command.Option
import pixel.nexa.network.command.OptionMapping

class DiscordOptionMapping(
    private val option: Option?,
    val optionMapping: net.dv8tion.jda.api.interactions.commands.OptionMapping
) :
    OptionMapping {
    override fun getOption() = option
    override fun asString() = optionMapping.asString
    override fun asInteger() = optionMapping.asLong
    override fun asNumber() = optionMapping.asDouble
    override fun asBoolean() = optionMapping.asBoolean
}
