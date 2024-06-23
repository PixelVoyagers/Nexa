package pixel.nexa.core.data.tag

interface ITaggable<T : ITag<*>> {

    fun toTag(): T

}