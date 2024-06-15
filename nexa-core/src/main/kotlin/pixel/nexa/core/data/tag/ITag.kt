package pixel.nexa.core.data.tag

import pixel.nexa.core.util.ICopyable

interface ITag<T> : ICopyable<ITag<T>> {

    fun read(): T

}