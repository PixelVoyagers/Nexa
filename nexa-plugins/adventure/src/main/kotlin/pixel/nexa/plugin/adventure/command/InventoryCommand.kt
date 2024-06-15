package pixel.nexa.plugin.adventure.command

import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.resource.AssetsMap
import pixel.nexa.network.command.Command
import pixel.nexa.network.command.NexaCommand
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.MutableMessageData
import pixel.nexa.network.session.CommandSession
import pixel.nexa.plugin.adventure.AdventurePlugin
import pixel.nexa.plugin.adventure.entity.item.Item
import pixel.nexa.plugin.adventure.handler.UserInventoryHandler

@Command("${AdventurePlugin.PLUGIN_ID}:inventory")
class InventoryCommand(private val assetsMap: AssetsMap, private val userInventoryHandler: UserInventoryHandler) : NexaCommand() {

    @Action
    suspend fun handle(@Argument session: CommandSession) {
        session.replyLazy {
            MutableMessageData().add(
                MessageFragments.pageView(
                    assetsMap.getPage(identifierOf("${AdventurePlugin.PLUGIN_ID}:inventory.html"))
                ) {
                    val language = session.getUser().getLanguageOrNull() ?: session.getLanguage()
                    put("language", language)
                    put("MessageFragments", MessageFragments)
                    put("ItemProperties", Item.Properties)
                    put("items", userInventoryHandler.getUserInventory(session.getUser()).getItems().chunked(1))
                    put("assetsMap", assetsMap)
                }
            )
        }
    }

}