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

    const val SMALL_UP_NUMBERS = "\u2070\u00B9\u00B2\u00B3\u2074\u2075\u2076\u2077\u2078\u2079"
    const val SMALL_DOWN_NUMBERS = "\u2080\u2081\u2082\u2083\u2084\u2085\u2086\u2087\u2088\u2089"

    fun toSmallUpNumbers(string: String) = string.map { SMALL_UP_NUMBERS.getOrNull(it.toString().toInt())?.toString() ?: it }.joinToString("")

    fun toSmallDownNumbers(string: String) = string.map { SMALL_DOWN_NUMBERS.getOrNull(it.toString().toInt())?.toString() ?: it }.joinToString("")

}