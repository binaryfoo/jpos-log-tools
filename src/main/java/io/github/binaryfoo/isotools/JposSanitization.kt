package io.github.binaryfoo.isotools

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

private val TIMESTAMP_FORMAT = DateTimeFormat.forPattern("MMM dd HH:mm:ss yyyy")

public fun parseTimestamp(s: String): DateTime {
    val dot = s.lastIndexOf('.')
    val millis = s.substring(dot + 1).toInt()
    return TIMESTAMP_FORMAT.parseDateTime(s.substring(4, dot).replace("EST ", "")).withMillisOfSecond(millis)
}