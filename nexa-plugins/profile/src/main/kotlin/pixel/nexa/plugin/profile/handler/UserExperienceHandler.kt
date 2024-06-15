package pixel.nexa.plugin.profile.handler

import pixel.auxframework.component.annotation.Component
import pixel.auxframework.core.registry.identifierOf
import pixel.nexa.core.data.component.IDataComponentType
import pixel.nexa.core.data.tag.ITag
import pixel.nexa.core.data.tag.NumberTag
import pixel.nexa.network.entity.user.User
import pixel.nexa.network.entity.user.UserDataSchema
import pixel.nexa.network.entity.user.editDataComponents
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.plugin.profile.ProfilePlugin
import pixel.nexa.plugin.profile.UserProfileEntries
import kotlin.math.max
import kotlin.random.Random
import kotlin.random.nextLong

class UserExperienceField : IDataComponentType<Long, ITag<Number>> {


    override fun deserialize(tag: ITag<Number>) = tag.read().toLong()
    override fun serialize(element: Long) = NumberTag<Number>(element)

}

@Component
class UserExperienceEntry(private val handler: UserExperienceHandler) : UserProfileEntries.UserProfileEntry {

    override fun getValue(user: User) = handler.getUserExperience(user).let { userExperience ->
        MessageFragments.text(
            "lv.${handler.getLevelByExperience(userExperience)} (${userExperience}/${
                handler.getExperienceByLevel(
                    handler.getLevelByExperience(userExperience) + 1
                )
            })"
        )
    }

    override fun isHidden(user: User) = handler.getUserExperience(user) <= 0

    override fun getName(user: User) = MessageFragments.translatable("text.profile.experience")

}

@Component
class UserExperienceHandler(userDataSchema: UserDataSchema) {

    val userExperienceField = UserExperienceField()

    init {
        userDataSchema.add(identifierOf("${ProfilePlugin.PLUGIN_ID}:experience") to userExperienceField)
    }

    fun setUserExperience(user: User, experience: Long) {
        user.editDataComponents {
            put(userExperienceField, max(experience, 0))
        }
    }

    fun getUserExperience(user: User) = user.getDataComponents().getOrPut(userExperienceField) {
        0
    }

    fun addUserExperience(user: User, experience: Long) = setUserExperience(user, getUserExperience(user) + experience)

    fun getLevelByExperience(exp: Long): Long {
        var currentExperience = 0L
        var level = 1L
        while (currentExperience + 1 < exp) {
            currentExperience += (5 * level) + 1
            level++
        }
        return level - 1
    }

    fun getExperienceByLevel(level: Long): Long {
        var experience = 1L
        for (i in 1 until level) {
            experience += (5 * i) + 1
        }
        return experience
    }

}

@Component
class ExperienceSignHandler(private val experienceHandler: UserExperienceHandler) : SignHandler.RewardSupplier {

    inner class ExperienceReward(val amount: Long) : SignHandler.Reward {

        override fun giveTo(user: User) {
            experienceHandler.addUserExperience(user, amount)
        }

        override fun getDisplay() = MessageFragments.translatable("text.profile.experience.amount", amount)

    }

    override fun getRewards(user: User) = setOf(ExperienceReward(Random.nextLong(2L..25L)))

}
