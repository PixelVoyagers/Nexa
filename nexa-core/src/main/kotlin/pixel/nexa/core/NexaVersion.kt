package pixel.nexa.core

import pixel.auxframework.util.FunctionUtils.memorize

class NexaVersion(val version: String) {

    companion object {
        fun current() = memorize {
            NexaVersion(NexaVersion::class.java.`package`.implementationVersion ?: "<null>")
        }
    }

    override fun hashCode() = version.hashCode()
    override fun equals(other: Any?) =
        other === this || (other != null && other is NexaVersion && other.version == this.version)

    override fun toString() = version

}
