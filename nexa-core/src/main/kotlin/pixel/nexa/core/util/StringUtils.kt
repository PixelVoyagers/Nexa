package pixel.nexa.core.util

object StringUtils {

    fun lowerCamelToLowerKebab(name: String): String {
        var result = ""
        for (i in name) {
            if (i.isUpperCase()) result += "-" + i.lowercase()
            else result += i
        }
        return result
    }

    fun lowerCamelToLowerUnderscore(name: String): String {
        var result = ""
        for (i in name) {
            if (i.isUpperCase()) result += "_" + i.lowercase()
            else result += i
        }
        return result
    }

}