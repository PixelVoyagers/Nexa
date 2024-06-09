package pixel.nexa.plugin.adapter.discord

import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import pixel.auxframework.component.factory.getComponent
import pixel.auxframework.core.registry.Identifier
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.NexaCore
import pixel.nexa.core.platform.NexaContext
import pixel.nexa.core.resource.AbstractLanguage
import pixel.nexa.core.resource.Languages
import pixel.nexa.network.command.CommandTranslator
import pixel.nexa.network.command.OptionTypes

object DiscordUtils {

    fun toDiscordOptionType(type: Identifier): OptionType = when (type) {
        identifierOf(OptionTypes.USER, NexaCore.DEFAULT_NAMESPACE) -> OptionType.USER
        identifierOf(OptionTypes.BOOLEAN, NexaCore.DEFAULT_NAMESPACE) -> OptionType.BOOLEAN
        identifierOf(OptionTypes.CHANNEL, NexaCore.DEFAULT_NAMESPACE) -> OptionType.CHANNEL
        identifierOf(OptionTypes.ATTACHMENT, NexaCore.DEFAULT_NAMESPACE) -> OptionType.ATTACHMENT
        identifierOf(OptionTypes.ROLE, NexaCore.DEFAULT_NAMESPACE) -> OptionType.ROLE
        identifierOf(OptionTypes.MENTION, NexaCore.DEFAULT_NAMESPACE) -> OptionType.MENTIONABLE
        identifierOf(OptionTypes.NUMBER, NexaCore.DEFAULT_NAMESPACE) -> OptionType.NUMBER
        identifierOf(OptionTypes.INTEGER, NexaCore.DEFAULT_NAMESPACE) -> OptionType.INTEGER
        else -> OptionType.STRING
    }


    fun CommandTranslator.putDiscordTranslations(commandData: CommandData, context: NexaContext) {
        val translation = this
        val languages = context.getAuxContext().componentFactory().getComponent<Languages>()
        for (locale in DiscordLocale.entries) {
            val nexaLanguage = locale.toNexa(context, default = languages.getRoot())
            if (nexaLanguage == languages.getRoot()) continue
            commandData.setNameLocalization(locale, translation.getCommandFullName().asText(nexaLanguage))
            if (commandData.type == Command.Type.SLASH && commandData is SlashCommandData) {
                commandData.setDescriptionLocalization(
                    locale,
                    translation.getCommandDescription().asText(nexaLanguage)
                )
                for (option in commandData.options) {
                    option.setNameLocalization(locale, translation.getOptionName(option.name).asText(nexaLanguage))
                    option.setDescriptionLocalization(
                        locale,
                        translation.getOptionDescription(option.name).asText(nexaLanguage)
                    )
                }
            }
        }
    }

    fun AbstractLanguage.toDiscord() = DiscordLocale.entries.firstOrNull {
        this.getLocaleTag(DiscordAdapter.PLATFORM_NAME) == it.locale
    } ?: DiscordLocale.UNKNOWN

    fun DiscordLocale.toNexa(context: NexaContext, default: AbstractLanguage? = null) = context.getAuxContext().componentFactory().getComponent<Languages>().let { languages ->
        languages.getLanguages().toList().firstOrNull {
            it.second.getLocaleTag(DiscordAdapter.PLATFORM_NAME) == this.locale
        }?.second ?: (default ?: languages.getDefault())
    }

}