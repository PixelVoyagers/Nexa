package pixel.nexa.network.message

interface GenericMessageEventHandler {

    fun handleGenericMessageEvent(messageSession: MessageSession)

}