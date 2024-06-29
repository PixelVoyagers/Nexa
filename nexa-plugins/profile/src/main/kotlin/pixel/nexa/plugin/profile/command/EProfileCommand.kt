package pixel.nexa.plugin.profile.command

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import pixel.auxframework.component.annotation.Autowired
import pixel.auxframework.component.annotation.Repository
import pixel.auxframework.component.factory.ComponentFactory
import pixel.auxframework.component.factory.getComponents
import pixel.auxframework.context.builtin.AfterContextRefreshed
import pixel.auxframework.context.builtin.SimpleListRepository
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.resource.AssetsMap
import pixel.nexa.network.command.Command
import pixel.nexa.network.command.CommandSession
import pixel.nexa.network.command.NexaCommand
import pixel.nexa.network.command.OptionTypes
import pixel.nexa.network.entity.user.User
import pixel.nexa.network.message.IDocumentSupport
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.MutableMessageData
import pixel.nexa.plugin.profile.ProfilePlugin
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Repository
abstract class UserProfileEntries : SimpleListRepository<UserProfileEntries.UserProfileEntry>, AfterContextRefreshed {

    @Autowired
    protected lateinit var componentFactory: ComponentFactory

    override fun afterContextRefreshed() {
        componentFactory.getComponents<UserProfileEntry>().forEach(::add)
    }

    interface UserProfileEntry {

        fun getName(user: User): IDocumentSupport

        fun isHidden(user: User) = false

        fun getValue(user: User): IDocumentSupport

    }

}

@Command("${ProfilePlugin.PLUGIN_ID}:e-profile")
class EProfileCommand(private val assetsMap: AssetsMap, private val userProfileEntries: UserProfileEntries) :
    NexaCommand() {

    @OptIn(ExperimentalEncodingApi::class)
    fun generateCode(user: String): String {
        val bitMatrix = MultiFormatWriter().encode(user, BarcodeFormat.CODE_128, 160, 100)
        bitMatrix.rotate(270)
        val image = BufferedImage(bitMatrix.width, bitMatrix.height, BufferedImage.TYPE_INT_RGB)
        for (x in 0 until image.width) {
            for (y in 0 until image.height) {
                image.setRGB(x, y, if (bitMatrix.get(x, y)) 0xFF00FFFF.toInt() else 0xFFFFFFFF.toInt())
            }
        }
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "PNG", outputStream)
        return "data:image/png;base64," + Base64.encode(outputStream.toByteArray())
    }

    @Action
    suspend fun handle(
        @Argument session: CommandSession,
        @Option(name = "user", type = OptionTypes.USER, required = false) input: String? = null,
        @Option(name = "hide-other", type = OptionTypes.BOOLEAN, required = false) hideOther: Boolean = false,
    ) {
        session.replyLazy {
            val userId = input ?: session.getUserId()
            val user = session.getBot().internal().getUserById(userId)
            MutableMessageData().add(
                MessageFragments.pageView(
                    assetsMap.getPage(identifierOf("${ProfilePlugin.PLUGIN_ID}:profile.html"))
                ) {
                    val language =
                        user.getLanguageOrNull() ?: session.getUser().getLanguageOrNull() ?: session.getLanguage()
                    put("language", language)
                    put("userName", user.getEffectiveName())
                    put("userId", userId)
                    put("userPlatform", user.getBot().getAdapter().getPlatform())
                    put("profileType", if (user.isBot()) "BOT" else "USER")
                    put(
                        "userAvatarUrl",
                        user.getEffectiveAvatarURL() ?: user.getAvatarURL() ?: user.getDefaultAvatarURL() ?: ""
                    )
                    put("userBarCode", generateCode("${user.getBot().getAdapter().getPlatform()}:${userId}"))
                    if (hideOther) put("userProfileEntries", emptyList<Any>())
                    else put(
                        "userProfileEntries",
                        userProfileEntries.getAll()
                            .mapNotNull {
                                if (it.isHidden(user)) return@mapNotNull null
                                else return@mapNotNull it.getName(user).asNode(language).toString() to it.getValue(user)
                                    .asNode(language).toString()
                            }
                            .chunked(3) {
                                Triple(it.first(), it.getOrNull(1), it.getOrNull(2))
                            }
                    )
                }
            )
        }
    }

}