package pixel.nexa.network.entity.user

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import pixel.auxframework.component.factory.getComponent
import pixel.auxframework.util.FunctionUtils.memorize
import pixel.nexa.core.NexaCore
import pixel.nexa.core.platform.adapter.NexaAdapter
import pixel.nexa.core.platform.adapter.NexaBot
import pixel.nexa.core.resource.AbstractLanguage
import pixel.nexa.core.resource.Languages
import pixel.nexa.core.util.FileStorage
import pixel.nexa.core.util.IStorage
import java.io.InputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

open class UserMeta {

    var locale: String? = null

}

abstract class User(private val bot: NexaBot<*>) {

    open fun getDataStorage(): IStorage<UserMeta> = memorize(this) {
        FileStorage(
            jacksonTypeRef<UserMeta>(),
            default = { UserMeta() },
            path = "user/${getUserInternalName()}.yml",
            getBot().getAdapter().getContext().getAuxContext().componentFactory().getComponent<NexaCore>()
        )
    }

    /**
     * 获取机器人实例
     */
    fun getBot() = bot

    /**
     * 获取用户ID
     */
    abstract fun getUserId(): String

    /**
     * 获取标准用户名称
     */
    abstract fun getUserName(): String

    /**
     * 获取用户名称
     */
    abstract fun getEffectiveName(): String

    /**
     * 获取标准头像字节流
     */
    abstract fun getAvatarStream(): InputStream?

    /**
     * 获取头像字节流
     */
    abstract fun getEffectiveAvatarStream(): InputStream?

    /**
     * 获取默认头像字节流
     */
    abstract fun getDefaultAvatarStream(): InputStream?

    /**
     * 获取标准头像URL
     */
    abstract fun getAvatarURL(): String?

    /**
     * 获取头像字节流
     */
    abstract fun getEffectiveAvatarURL(): String?

    /**
     * 获取默认头像字节流
     */
    abstract fun getDefaultAvatarURL(): String?

    open fun getLanguageOrNull() = getDataStorage().get().locale?.let {
        getBot().getAdapter().getContext().getAuxContext().componentFactory().getComponent<Languages>().getLanguageOrNull(it)
    }

    fun getLanguage() = getLanguageOrNull()!!

    open fun setLanguage(language: AbstractLanguage) = apply {
        val dataHolder = getDataStorage()
        val data = dataHolder.get()
        data.locale = getBot().getAdapter().getContext().getAuxContext().componentFactory().getComponent<Languages>().getName(language)
        dataHolder.set(data)
    }

    open fun isBot(): Boolean = getBot().getAdapter().getContext().getAdapters().flatMap(NexaAdapter<*, *>::getBots).firstOrNull {
        "${it.getAdapter().getPlatform()}:${it.getSelfId()}" == "${getBot().getAdapter().getPlatform()}:${getUserId()}"
    } != null

}

@OptIn(ExperimentalEncodingApi::class)
fun User.getUserInternalName() = Base64.UrlSafe.encode("${getBot().getAdapter().getBotInternalName(getBot())}:${getUserId()}".toByteArray())
