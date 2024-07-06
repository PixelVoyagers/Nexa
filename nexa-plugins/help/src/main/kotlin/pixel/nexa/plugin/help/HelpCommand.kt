package pixel.nexa.plugin.help

import pixel.auxframework.component.annotation.Component
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.resource.asset.AbstractLanguage
import pixel.nexa.core.resource.asset.AssetsMap
import pixel.nexa.network.command.*
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.MutableMessageData

@Component
@Command("help:help")
class HelpCommand(private val commandService: CommandService, private val assetsMap: AssetsMap) : NexaCommand() {

    @Action
    suspend fun handle(
        @Option(
            "command",
            required = false,
            autoComplete = Option.AutoCompleteMode.ENABLED
        ) commandName: String? = null, @Argument session: CommandSession
    ): Any {
        val locale: AbstractLanguage = session.getUser().getLanguageOrNull() ?: session.getLanguage()
        val commands = commandService.getCommands()
        return if (commandName != null)
            handleCommandHelp(commands.first {
                it.getCommandData().getIdentifier() == identifierOf(
                    commandName,
                    "nexa"
                )
            }, session, locale)
        else {
            session.replyLazy {
                MutableMessageData().add(
                    MessageFragments.pageView(
                        assetsMap.getPage(identifierOf("help:help.html"))
                    ) {
                        put("language", locale)
                        put("commands", commands)
                    }
                )
            }
        }
    }

    suspend fun handleCommandHelp(command: NexaCommand, session: CommandSession, locale: AbstractLanguage) =
        session.replyLazy {
            MutableMessageData().add(
                MessageFragments.pageView(
                    assetsMap.getPage(identifierOf("help:command.html"))
                ) {
                    put("language", locale)
                    put("command", command)
                }
            )
        }

    @AutoComplete("command")
    fun autoComplete(event: CommandAutoComplete) {
        event.result += commandService.getCommands().map {
            CommandAutoComplete.Choice(
                "${
                    it.commandTranslator.getCommandName().asText(event.user.getLanguageOrNull() ?: event.language)
                } (${it.getCommandData().getIdentifier()})",
                it.getCommandData().getIdentifier().toString()
            )
        }
    }

}
