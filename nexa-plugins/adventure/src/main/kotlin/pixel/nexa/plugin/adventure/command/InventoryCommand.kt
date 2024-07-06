package pixel.nexa.plugin.adventure.command

import pixel.auxframework.component.annotation.Component
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.resource.asset.AssetsMap
import pixel.nexa.network.command.*
import pixel.nexa.network.entity.user.User
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.MutableMessageData
import pixel.nexa.plugin.adventure.AdventurePlugin
import pixel.nexa.plugin.adventure.entity.AdventureRegistries
import pixel.nexa.plugin.adventure.entity.fluid.Fluid
import pixel.nexa.plugin.adventure.entity.fluid.FluidStack
import pixel.nexa.plugin.adventure.entity.item.Item
import pixel.nexa.plugin.adventure.entity.item.ItemStack
import pixel.nexa.plugin.adventure.handler.UserInventoryHandler
import kotlin.math.max
import kotlin.math.min

@Component
@Command("${AdventurePlugin.PLUGIN_ID}:inventory")
class InventoryCommand(
    private val assetsMap: AssetsMap,
    private val userInventoryHandler: UserInventoryHandler,
    private val adventureRegistries: AdventureRegistries
) : NexaCommand() {

    fun getItemChunks(user: User) = userInventoryHandler.getUserInventory(user).getItems().chunked(2).chunked(3)
    fun getFluidChunks(user: User) = userInventoryHandler.getUserInventory(user).getFluids().chunked(2).chunked(3)


    @AutoComplete("page")
    fun autoComplete(autoComplete: CommandAutoComplete) {
        val itemChunks = getItemChunks(autoComplete.user)
        val fluidChunks = getFluidChunks(autoComplete.user)
        val range = 0..<max(itemChunks.size, fluidChunks.size)
        autoComplete.result += range.map { CommandAutoComplete.Choice((it + 1).toString()) }
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Action
    suspend fun handle(
        @Argument session: CommandSession,
        @Option(required = false, autoComplete = Option.AutoCompleteMode.ENABLED) page: Int = 1
    ) {
        session.replyLazy {
            val itemChunks = getItemChunks(session.getUser())
            val fluidChunks = getFluidChunks(session.getUser())
            val pageIndex = max(min(max(0, page - 1), max(itemChunks.size, fluidChunks.size) - 1), 0)
            MutableMessageData().add(
                MessageFragments.pageView(
                    assetsMap.getPage(identifierOf("${AdventurePlugin.PLUGIN_ID}:inventory.html"))
                ) {
                    val language = session.getUser().getLanguageOrNull() ?: session.getLanguage()
                    put("page", "${pageIndex + 1} / ${max(max(itemChunks.size, fluidChunks.size), 1)}")
                    put("language", language)
                    put("MessageFragments", MessageFragments)
                    put("ItemProperties", Item.Properties)
                    put("FluidProperties", Fluid.Properties)
                    put("items", itemChunks.getOrNull(pageIndex) ?: emptyList<List<List<List<ItemStack>>>>())
                    put("fluids", fluidChunks.getOrNull(pageIndex) ?: emptyList<List<List<List<FluidStack>>>>())
                    put("assetsMap", assetsMap)
                    put("maxInt", { a: Int, b: Int -> max(a, b) })
                    put("identifierOf", { a: String -> identifierOf(a) })
                    put(
                        "toHexString",
                        { int: Int -> int.toHexString(HexFormat.UpperCase).let { it.substring(2, it.length) } })
                }
            )
        }
    }

}