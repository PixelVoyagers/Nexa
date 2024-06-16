package pixel.nexa.plugin.adventure.command

import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.data.component.DataCompoundMapParser
import pixel.nexa.network.command.Command
import pixel.nexa.network.command.CommandAutoComplete
import pixel.nexa.network.command.NexaCommand
import pixel.nexa.network.command.OptionTypes
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.MutableMessageData
import pixel.nexa.network.session.CommandSession
import pixel.nexa.plugin.adventure.AdventurePlugin
import pixel.nexa.plugin.adventure.entity.AdventureRegistries
import pixel.nexa.plugin.adventure.entity.item.ItemStack
import pixel.nexa.plugin.adventure.handler.UserInventoryHandler

@Command("${AdventurePlugin.PLUGIN_ID}:give-item", needPermission = true)
class GiveItemCommand(private val adventureRegistries: AdventureRegistries, private val inventoryHandler: UserInventoryHandler) : NexaCommand() {

    @Action
    suspend fun handle(
        @Argument session: CommandSession,
        @Option(autoComplete = Option.AutoCompleteMode.ENABLED) id: String,
        @Option(type = OptionTypes.INTEGER, required = false) amount: Long = 1,
        @Option(type = OptionTypes.USER, required = false) userId: String = session.getUserId(),
        @Option(required = false) data: String? = null
    ) {
        val user = session.getBot().internal().getUserById(userId)
        session.replyLazy {
            val item = adventureRegistries.items.get(identifierOf(id))!!
            val stack = when (data) {
                null -> ItemStack(item, amount)
                else -> ItemStack(
                    item,
                    amount,
                    DataCompoundMapParser.parseDataComponentMap(data, item.getDataComponentSchema())
                )
            }
            inventoryHandler.editUserInventory(user) {
                give(stack)
            }
            MutableMessageData().add(
                MessageFragments.text("√")
            )
        }
    }

    @AutoComplete("id")
    fun autoComplete(complete: CommandAutoComplete) {
        complete.result += adventureRegistries.items.toList().map {
            CommandAutoComplete.Choice(
                "${it.second.getName(it.second.stackTo()).asText(complete.user.getLanguageOrNull() ?: complete.language)} (${it.first})"
            )
        }
    }

}