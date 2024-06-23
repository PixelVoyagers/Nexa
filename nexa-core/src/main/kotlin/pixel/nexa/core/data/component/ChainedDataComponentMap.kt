package pixel.nexa.core.data.component

import pixel.auxframework.core.registry.Identifier
import pixel.nexa.core.data.tag.ITag

abstract class ChainedDataComponentMap {

    protected abstract fun getMap(): DataComponentMap

    fun <E, T : ITag<E>> component(type: Pair<Identifier, IDataComponentType<E, T>>) =
        getMap().getTyped(type.second)?.getOrNull()

    fun <E, T : ITag<E>> component(type: IDataComponentType<E, T>) = getMap().getTyped(type)?.getOrNull()
    fun <E, T : ITag<E>> component(name: Identifier) = getMap().get<E, T>(name)?.get()?.getOrNull()

    fun <E, T : ITag<E>> component(type: IDataComponentType<E, T>, value: E) = apply {
        getMap().put(type, value)
    }

    fun <E, T : ITag<E>> component(type: Pair<Identifier, IDataComponentType<E, T>>, value: E) =
        component(type.second, value)

    fun <E, T : ITag<E>> component(name: Identifier, value: E) = apply {
        getMap().put<E, T>(name, value)
    }

}