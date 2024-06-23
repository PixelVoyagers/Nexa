package pixel.nexa.core.data.component

import pixel.nexa.core.data.tag.ITag

interface IDataComponentType<E, T : ITag<*>> {

    fun deserialize(tag: T): E
    fun serialize(element: E): T

}