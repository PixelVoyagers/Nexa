package pixel.nexa.core.platform.adapter

interface NexaBot<S : NexaBot<S>> {

    interface Internal

    fun getAdapter(): NexaAdapter<S, *>

    fun getName(): String
    fun setName(name: String)

    fun internal(): Internal

    fun start()
    fun stop()

}