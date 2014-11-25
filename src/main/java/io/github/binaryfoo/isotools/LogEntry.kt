package io.github.binaryfoo.isotools

import java.util.regex.Pattern
import java.util.LinkedHashMap
import java.util.ArrayList
import kotlin.properties.Delegates
import java.util.HashSet
import java.util.HashMap

public data class LogEntry(private val _fields: Map<String, String>,
                           public val rootAttributes: Map<String, String> = mapOf()) {

    public fun get(idPath: String): String? {
        val v = _fields[idPath]
        return v ?: rootAttributes[idPath]
    }

    public val at: String by Delegates.mapVal(rootAttributes)
    public val realm: String by Delegates.mapVal(rootAttributes)

    public val fields: Set<Pair<String, String>> by Delegates.lazy {
        HashSet(_fields.map { Pair(it.key, it.value) })
    }

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
    if (rootAttributes == null) {
        throw IllegalArgumentException("No <log found in record: ${lines.join("\n")}")
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

public fun List<LogEntry>.filter(fields: Set<Pair<String, String>>): List<LogEntry> {
    return filter { it.fields.containsAll(fields) }
}

public fun LogEntry.hasField(id: String, value: String): Boolean = this[id] == value

public fun List<LogEntry>.pairRequestWithResponse(): List<EntryPair> {
    val pending = HashMap<String, LogEntry>()
    val matches = ArrayList<EntryPair>()
    forEach { e ->
        val mti = e["0"]
        if (mti != null) {
            val key = normaliseToRequestMTI(mti) + "-" + e["11"]
            val match = pending[key]
            if (match != null) {
                val pair = if (isResponseMTI(mti)) EntryPair(match, e) else EntryPair(e, match)
                matches.add(pair)
                pending.remove(key)
            } else {
                pending.put(key, e)
            }
        }
    }
    return matches;
}
