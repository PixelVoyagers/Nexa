package pixel.nexa.core.platform.adapter

abstract class AbstractNexaBot<S : NexaBot<S>> : NexaBot<S> {

    class Internal(val nexaBot: NexaBot<*>) : NexaBot.Internal

    private var name = "Bot"
    override fun getName() = name
    override fun setName(name: String) {
        this.name = name
    }

    override fun start() {}
    override fun stop() {}

    override fun internal() = Internal(this)

}