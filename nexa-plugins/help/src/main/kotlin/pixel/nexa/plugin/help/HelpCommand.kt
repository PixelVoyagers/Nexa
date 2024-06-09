package pixel.nexa.plugin.help

import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.resource.AbstractLanguage
import pixel.nexa.core.resource.AssetsMap
import pixel.nexa.network.command.Command
import pixel.nexa.network.command.CommandAutoComplete
import pixel.nexa.network.command.CommandContainer
import pixel.nexa.network.command.NexaCommand
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.MutableMessageData
import pixel.nexa.network.session.CommandSession

@Command("help:help")
class HelpCommand(private val commandContainer: CommandContainer, private val assetsMap: AssetsMap) : NexaCommand() {

    @Action
    suspend fun handle(@Option("command", required = false, autoComplete = Option.AutoCompleteMode.ENABLED) commandName: String? = null, @Argument session: CommandSession): Any {
        val locale: AbstractLanguage = session.getUser().getLanguageOrNull() ?: session.getLanguage()
        return if (commandName != null)
            handleCommandHelp(commandContainer.getAll().first { it.getCommandData().getIdentifier() == identifierOf(commandName, "nexa") }, session, locale)
        else {
            val commands = commandContainer.getAll()
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

    suspend fun handleCommandHelp(command: NexaCommand, session: CommandSession, locale: AbstractLanguage) = session.replyLazy {
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
        event.result += commandContainer.getAll().map {
            CommandAutoComplete.Choice(it.getCommandData().getIdentifier().toString())
        }
    }

}
