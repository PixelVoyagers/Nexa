package pixel.nexa.network.message

/**
 * 消息数据
 */
interface MessageData : Iterable<MessageFragment>

/**
 * 转为可变消息数据
 */
fun MessageData.toMutable() = MutableMessageData(this.toMutableList())
