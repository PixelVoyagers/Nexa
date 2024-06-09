package pixel.nexa.network.command

import pixel.auxframework.core.registry.Identifier
import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.TextFragment
import java.util.*

open class CommandTranslator(private val identifier: Identifier) {

    /**
     * 获取指令标识
     */
    fun getIdentifier() = identifier

    /**
     * 获取本地化指令名称
     */
    open fun getCommandName() = MessageFragments.translatable(
        "command.${getIdentifier().getNamespace()}.${getIdentifier().getPath().split("/")[0]}.name"
    ) { MessageFragments.literal(getIdentifier().getPath().split("/")[0]) }

    /**
     * 获取本地化选项名称
     */
    open fun getOptionName(name: String) = MessageFragments.translatable(
        "command.${getIdentifier().getNamespace()}.${
            getIdentifier().getPath().split("/").joinToString("-")
        }.option.$name.name"
    ) { MessageFragments.literal(name) }

    /**
     * 获取本地化指令全称
     */
    open fun getCommandFullName(): TextFragment {
        val fragments = mutableListOf<TextFragment>()
        fragments += getCommandName()
        val subNames = getCommandSubNames()
        if (subNames.isNotEmpty()) for (name in subNames) {
            fragments += MessageFragments.literal("-")
            fragments += name
        }
        return MessageFragments.multiple(*fragments.toTypedArray())
    }

    /**
     * 获取本地化指令子名称
     */
    open fun getCommandSubNames(): Array<TextFragment> {
        val location = getIdentifier()
        val split = getIdentifier().getPath().split("/")
        return if (split.size > 1) (
                split.subList(1, split.size).stream()
                    .map { name ->
                        MessageFragments.translatable(
                            "command.%s.%s.subName.%s".format(
                                Locale.ROOT,
                                location.getNamespace(),
                                split[0],
                                name
                            )
                        ) { MessageFragments.literal(name) }
                    }
                    .toList()
                ).toTypedArray() else arrayOf()
    }

    /**
     * 获取本地化指令介绍
     */
    open fun getCommandDescription() = MessageFragments.translatable(
        "command.${getIdentifier().getNamespace()}.${
            getIdentifier().getPath().split("/").joinToString("-")
        }.description"
    ) { MessageFragments.literal("-") }

    /**
     * 获取本地化标签介绍
     */
    open fun getOptionDescription(name: String) = MessageFragments.translatable(
        "command.${getIdentifier().getNamespace()}.${
            getIdentifier().getPath().split("/").joinToString("-")
        }.option.$name.description"
    ) { MessageFragments.literal("-") }

}
