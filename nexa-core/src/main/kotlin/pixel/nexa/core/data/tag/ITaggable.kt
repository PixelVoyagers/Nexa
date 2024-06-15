package kodash.data.tag

import pixel.nexa.core.data.tag.ITag

interface ITaggable<T : ITag<*>> {

    fun toTag(): T

}