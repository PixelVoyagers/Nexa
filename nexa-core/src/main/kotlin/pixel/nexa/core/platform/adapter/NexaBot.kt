package pixel.nexa.core.platform.adapter

import pixel.nexa.network.entity.user.User

interface NexaBot<S : NexaBot<S>> {

    interface Internal {

        suspend fun getUserById(id: String): User

    }

    fun getSelfId(): String

    fun getAdapter(): NexaAdapter<S, *>

    fun getName(): String
    fun setName(name: String)

    fun internal(): Internal

    fun start()
    fun stop()

}