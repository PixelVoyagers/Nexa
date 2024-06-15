package pixel.nexa.network.entity.user

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import pixel.auxframework.component.annotation.Repository
import pixel.auxframework.component.factory.AfterComponentAutowired
import pixel.auxframework.component.factory.getComponent
import pixel.auxframework.context.builtin.SimpleListRepository
import pixel.auxframework.core.registry.Identifier
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.NexaCore
import pixel.nexa.core.data.component.DataComponentMap
import pixel.nexa.core.data.component.IDataComponentType
import pixel.nexa.core.data.component.ListDataType
import pixel.nexa.core.data.component.StringDataType
import pixel.nexa.core.data.tag.CompoundTag
import pixel.nexa.core.data.tag.compoundTagOf
import pixel.nexa.core.platform.adapter.NexaAdapter
import pixel.nexa.core.platform.adapter.NexaBot
import pixel.nexa.core.resource.AbstractLanguage
import pixel.nexa.core.resource.Languages
import pixel.nexa.core.util.DataTypes
import pixel.nexa.core.util.FileStorage
import pixel.nexa.core.util.IStorage
import java.io.InputStream
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

open class UserMeta {

    var locale: String? = null

    var dataComponent: MutableMap<String, Any?> = mutableMapOf()
    var compoundTag: MutableMap<String, Any?> = mutableMapOf()

}

@Repository
abstract class UserDataSchema : SimpleListRepository<Pair<Identifier, IDataComponentType<*, *>>>, AfterComponentAutowired {

    companion object {

        val FIELD_PERMISSIONS = identifierOf("permissions", NexaCore.DEFAULT_NAMESPACE) to ListDataType(StringDataType())

    }

    override fun afterComponentAutowired() {
        add(FIELD_PERMISSIONS)
    }

}

abstract class User(private val bot: NexaBot<*>) {

    private var compoundTag: CompoundTag? = null
    private var dataComponents: DataComponentMap? = null
    private var dataStorage: IStorage<UserMeta>? = null

    private val nexaCore = bot.getAdapter().getContext().getAuxContext().componentFactory().getComponent<NexaCore>()
    private val dataComponentSchema = bot.getAdapter().getContext().getAuxContext().componentFactory().getComponent<UserDataSchema>()

    open fun getDataComponents() = dataComponents!!
    open fun setDataComponents(map: DataComponentMap) = editData {
        dataComponent = map.read().read().toMutableMap()
    }

    open fun getCompoundTag() = compoundTag!!
    open fun setCompoundTag(tag: CompoundTag) = editData {
        compoundTag = tag.read().toMutableMap()
    }

    open fun getDataStorage(): IStorage<UserMeta> = dataStorage!!

    open fun refresh() {
        if (dataStorage == null)
            dataStorage = FileStorage(
                jacksonTypeRef<UserMeta>(),
                default = { UserMeta() },
                path = "user/${getUserInternalName()}.bson",
                nexaCore,
                type = DataTypes.BSON
            )
        val got = getDataStorage().get()
        compoundTag = compoundTagOf(got.compoundTag)
        dataComponents = DataComponentMap().apply {
            schema += dataComponentSchema.getAll()
            load(compoundTagOf(got.dataComponent))
        }
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
     * 获取标准头像Url
     */
    abstract fun getAvatarURL(): String?

    /**
     * 获取头像Url
     */
    abstract fun getEffectiveAvatarURL(): String?

    /**
     * 获取默认头像Url
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

fun User.editData(block: UserMeta.() -> Unit) = apply {
    val holder = getDataStorage()
    val data = holder.get()
    data.block()
    holder.set(data)
    refresh()
}

fun User.editCompoundTag(block: CompoundTag.() -> Unit) = apply {
    val tag = getCompoundTag()
    tag.block()
    setCompoundTag(tag)
}


fun User.editDataComponents(block: DataComponentMap.() -> Unit) = apply {
    val tag = getDataComponents()
    tag.block()
    setDataComponents(tag)
}
