package io.github.binaryfoo.isotools

import java.util.regex.Pattern

public fun normaliseToRequestMTI(mti: String): String {
    if (mti.length == 4) {
        if (isResponseMTI(mti)) {
            val characters = mti.toCharArray()
            characters[2] = (mti[2].toInt() - 1).toChar()
            return String(characters)
        }
    }
    return mti
}

public fun isResponseMTI(mti: String): Boolean {
    val third = mti[2].toInt()
    return third % 2 == 1
}
