package pixel.nexa.network.message

/**
 * 可变的消息数据
 */
open class MutableMessageData(private val list: MutableList<MessageFragment> = mutableListOf()) : MessageData, Iterable<MessageFragment> by list {

    /**
     * 添加片段
     */
    open fun add(fragment: MessageFragment) = this.also {
        list.add(fragment)
    }

    /**
     * 根据元素删除片段
     */
    open fun remove(fragment: MessageFragment) = this.also {
        list.remove(fragment)
    }

    /**
     * 根据索引删除片段
     */
    open fun remove(at: Int) = this.also {
        list.removeAt(at)
    }

    /**
     * 增加片段
     */
    open operator fun plusAssign(fragment: MessageFragment) = list.plusAssign(fragment)

    /**
     * 增加片段
     */
    open operator fun plusAssign(fragments: Iterable<MessageFragment>) = fragments.forEach { this += it }

    /**
     * 获取片段
     */
    open operator fun get(at: Int) = list[at]

    /**
     * 判断片段是否存在
     */
    open operator fun contains(fragment: MessageFragment) = list.contains(fragment)

    val size: Int get() = list.size

}