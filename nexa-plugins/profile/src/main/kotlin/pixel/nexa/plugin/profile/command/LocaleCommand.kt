package pixel.nexa.plugin.profile.command

import pixel.nexa.core.resource.Languages
import pixel.nexa.network.command.*
import pixel.nexa.network.message.MutableMessageData
import pixel.nexa.plugin.profile.ProfilePlugin
import java.text.DecimalFormat

@Command("${ProfilePlugin.PLUGIN_ID}:locale")
class LocaleCommand(private val languages: Languages) : NexaCommand() {

    @Action
    suspend fun handle(
        @Argument session: CommandSession,
        @Option(
            "language",
            autoComplete = Option.AutoCompleteMode.ENABLED,
            required = false
        ) languageName: String?
    ): Any {
        if (languageName == null)
            return session.reply(
                languages.getName(session.getUser().getLanguageOrNull() ?: languages.getDefault()) ?: "null"
            )
        val language = languages.getLanguageOrNull(languageName) ?: return session.reply(
            MutableMessageData().add(
                translatableReply("not-found", languageName)
            )
        )
        session.getUser().setLanguage(language)
        return session.reply(MutableMessageData().add(translatableReply("success", languageName)))
    }

    @AutoComplete("language")
    fun autoComplete(autoComplete: CommandAutoComplete) {
        val decimalFormat = DecimalFormat("0.00")
        autoComplete.result += languages.getLanguages().map {
            CommandAutoComplete.Choice(
                "${it.key} (${decimalFormat.format(it.value.getCompletionRate(languages.getDefault()) * 100F)}%)",
                it.key
            )
        }
    }

}
