package pixel.nexa.network.command

import pixel.nexa.network.message.MessageFragments
import pixel.nexa.network.message.TextFragment

/**
 * 简单本地化指令回复
 */
fun NexaCommand.translatableReply(name: String, vararg args: Any, fallback: (() -> TextFragment)? = null) =
    this.getCommandData().getIdentifier().format { namespace, path ->
        "command.$namespace.${path.split("/").joinToString("-")}.reply.$name"
    }.let { MessageFragments.translatable(it, *args, fallback ?: { MessageFragments.literal(it) }) }
