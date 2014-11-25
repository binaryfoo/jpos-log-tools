package io.github.binaryfoo.isotools

import java.util.regex.Pattern
import java.util.LinkedHashMap
import java.util.ArrayList
import kotlin.properties.Delegates

public data class LogEntry(private val fields: Map<String, String>,
                           public val rootAttributes: Map<String, String>) {

    public fun get(idPath: String): String? = fields[idPath]

    public val at: String by Delegates.mapVal(rootAttributes)
    public val realm: String by Delegates.mapVal(rootAttributes)

}

public fun fromLines(lines: Array<String>): LogEntry = fromLines(lines.stream())

public fun fromLines(lines: Iterable<String>): LogEntry = fromLines(lines.stream())

public fun fromLines(lines: Stream<String>): LogEntry {
    fun List<String>.endingAt(id: String) = (this + id).join(".")

    var rootAttributes: Map<String, String>? = null
    val path = ArrayList<String>()
    val fields: MutableMap<String, String> = LinkedHashMap()
    lines.forEach {
        val attributes = extractAttributes(it)
        if (it.contains("<field")) {
            val id = attributes["id"]!!
            val value = attributes["value"]!!
            fields.put(path.endingAt(id), value)
        } else if (it.contains("<isomsg ")) {
            val id = attributes["id"]
            if (id != null) {
                path.add(id)
            }
        } else if (it.contains("<log ")) {
            rootAttributes = attributes
        } else if (it.contains("</isomsg>") && path.notEmpty) {
            path.remove(path.lastIndex)
        }
    }
    return LogEntry(fields, rootAttributes!!)
}

private val ID_VAL_PATTERN = Pattern.compile("(\\w+)=\"([^\"]+)\"")

fun extractAttributes(line: String): Map<String, String> {
    val attrs = LinkedHashMap<String, String>()
    val matcher = ID_VAL_PATTERN.matcher(line)
    while (matcher.find()) {
        val name = matcher.group(1)
        val value = matcher.group(2)
        attrs[name] = value
    }
    return attrs
}

