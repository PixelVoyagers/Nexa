package pixel.nexa.core.util

object TypeUtils {

    inline fun <I, reified O> I.convertIfNotOfType(block: (I) -> O) = when (this) {
        is O -> this
        else -> block(this)
    }

}