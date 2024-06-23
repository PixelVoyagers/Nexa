package pixel.nexa.plugin.profile.handler

import pixel.auxframework.component.annotation.Autowired
import pixel.auxframework.component.annotation.Component
import pixel.auxframework.component.annotation.Repository
import pixel.auxframework.component.factory.ComponentFactory
import pixel.auxframework.component.factory.getComponents
import pixel.auxframework.context.builtin.AfterContextRefreshed
import pixel.auxframework.context.builtin.SimpleListRepository
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.data.component.IDataComponentType
import pixel.nexa.core.data.tag.ITag
import pixel.nexa.core.data.tag.NumberTag
import pixel.nexa.network.entity.user.User
import pixel.nexa.network.entity.user.UserDataSchema
import pixel.nexa.network.entity.user.editDataComponents
import pixel.nexa.network.message.TextFragment
import pixel.nexa.plugin.profile.ProfilePlugin
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class UserLastSignField : IDataComponentType<Long, ITag<Number>> {


    override fun deserialize(tag: ITag<Number>) = tag.read().toLong()
    override fun serialize(element: Long) = NumberTag<Number>(element)

}

@Repository
abstract class SignRewardSupplierRepository : SimpleListRepository<SignHandler.RewardSupplier>, AfterContextRefreshed {

    @Autowired
    protected lateinit var componentFactory: ComponentFactory

    override fun afterContextRefreshed() {
        componentFactory.getComponents<SignHandler.RewardSupplier>().forEach(::add)
    }

}

@Component
class SignHandler(private val repository: SignRewardSupplierRepository, userDataSchema: UserDataSchema) {

    val userLastSignField = UserLastSignField()

    init {
        userDataSchema.add(identifierOf("${ProfilePlugin.PLUGIN_ID}:last_sign") to userLastSignField)
    }

    interface RewardSupplier {

        fun getRewards(user: User): Set<Reward>

    }

    interface Reward {

        fun giveTo(user: User)
        fun getDisplay(): TextFragment

    }

    fun getUserLastSign(user: User): LocalDateTime? =
        user.getDataComponents().getTyped(userLastSignField)?.getOrNull()?.let {
            Instant.ofEpochSecond(it).atOffset(ZoneOffset.UTC).toLocalDateTime()
        }


    fun setUserLastSign(user: User, dateTime: LocalDateTime) = user.editDataComponents {
        put(userLastSignField, dateTime.toEpochSecond(ZoneOffset.UTC))
    }

    fun randomRewards(user: User, amount: Int = 10) = repository
        .getAll()
        .flatMap { it.getRewards(user) }
        .shuffled()
        .take(amount)

}
