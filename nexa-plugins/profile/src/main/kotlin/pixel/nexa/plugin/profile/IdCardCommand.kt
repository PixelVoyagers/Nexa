package pixel.nexa.plugin.profile

import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.resource.AssetsMap
import pixel.nexa.network.command.Command
import pixel.nexa.network.command.NexaCommand
import pixel.nexa.network.command.OptionTypes
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.MutableMessageData
import pixel.nexa.network.session.CommandSession
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@Command("profile:id-card")
class IdCardCommand(private val assetsMap: AssetsMap) : NexaCommand() {

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
        @Option(name = "user", type = OptionTypes.USER, required = false) input: String? = null
    ) {
        val userId = input ?: session.getUserId()
        val user = session.getBot().internal().getUserById(userId)
        session.replyLazy {
            MutableMessageData().add(
                MessageFragments.pageView(
                    assetsMap.getPage(identifierOf("profile:profile.html"))
                ) {
                    put("language", user.getLanguageOrNull() ?: session.getUser().getLanguageOrNull() ?: session.getLanguage())
                    put("userName", user.getEffectiveName())
                    put("userId", userId)
                    put("userPlatform", user.getBot().getAdapter().getPlatform())
                    put("profileType", if (user.isBot()) "BOT" else "USER")
                    put(
                        "userAvatarUrl",
                        user.getEffectiveAvatarURL() ?: user.getAvatarURL() ?: user.getDefaultAvatarURL() ?: ""
                    )
                    put("userBarCode", generateCode("${user.getBot().getAdapter().getPlatform()}:${userId}"))
                }
            )
        }
    }

}