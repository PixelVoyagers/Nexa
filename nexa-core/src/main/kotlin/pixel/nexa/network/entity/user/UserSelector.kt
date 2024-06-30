package pixel.nexa.network.entity.user

import pixel.auxframework.component.factory.getComponent
import pixel.nexa.core.platform.NexaContext
import java.nio.CharBuffer

abstract class UserSelector {

    abstract suspend fun matchUsers(): Set<User>

    companion object {

        /**
         * 选取所有机器人实例缓存中的所有用户
         */
        const val ALL = "nexa:all"

        context(NexaContext)
        fun parse(input: String): UserSelector = when {
            input == ALL -> AllUserSelector(getNexaContext())
            input.startsWith("[") && input.endsWith("]") -> UserSelectorList.parse(input)
            else -> UserSelectorList()
        }

    }

}

class UserSelectorList(private vararg val user: UserSelector) : UserSelector() {

    companion object {

        context(NexaContext)
        fun parse(raw: String): UserSelectorList {
            val buffer = CharBuffer.wrap(raw.removePrefix("[").removeSuffix("]"))
            val list = mutableListOf<String>()
            var current = ""
            while (true) {
                val got = buffer.get()
                if (got.isWhitespace()) continue
                if (got == ',') {
                    list += current
                    current = ""
                }
                if (got == '[') return parse("[${buffer}")
                else if (got == ']') break
                else current += got
            }
            list += current
            return UserSelectorList(*list.filter(String::isNotEmpty).map { UserSelector.parse(it) }.toTypedArray())
        }

    }

    override suspend fun matchUsers() = user.flatMap { it.matchUsers() }.toSet()

}

class AllUserSelector(private val nexaContext: NexaContext) : UserSelector() {

    override suspend fun matchUsers() = nexaContext.getAuxContext().componentFactory().getComponent<NexaContextUsers>().toSet()

}
