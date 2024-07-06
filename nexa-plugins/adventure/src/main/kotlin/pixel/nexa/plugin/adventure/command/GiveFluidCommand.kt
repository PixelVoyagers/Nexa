package pixel.nexa.plugin.adventure.command

import pixel.auxframework.component.annotation.Component
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.data.component.DataCompoundMapParser
import pixel.nexa.network.command.*
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.MutableMessageData
import pixel.nexa.plugin.adventure.AdventurePlugin
import pixel.nexa.plugin.adventure.entity.AdventureRegistries
import pixel.nexa.plugin.adventure.entity.fluid.FluidStack
import pixel.nexa.plugin.adventure.handler.UserInventoryHandler

@Component
@Command("${AdventurePlugin.PLUGIN_ID}:give-fluid", needPermission = true)
class GiveFluidCommand(
    private val adventureRegistries: AdventureRegistries,
    private val inventoryHandler: UserInventoryHandler
) : NexaCommand() {

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
            val fluid = adventureRegistries.fluids.get(identifierOf(id))!!
            val stack = when (data) {
                null -> FluidStack(fluid, amount)
                else -> FluidStack(
                    fluid,
                    amount,
                    DataCompoundMapParser.parseDataComponentMap(data, fluid.getDataComponentSchema())
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
        complete.result += adventureRegistries.fluids.toList().map {
            CommandAutoComplete.Choice(
                "${
                    it.second.getName(it.second.stackTo())
                        .asText(complete.user.getLanguageOrNull() ?: complete.language)
                } (${it.first})",
                it.first.toString()
            )
        }
    }

}