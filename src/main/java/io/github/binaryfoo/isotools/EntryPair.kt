package io.github.binaryfoo.isotools

import java.util.ArrayList

public data class EntryPair(public val request: LogEntry?, public val response: LogEntry?) {

    public fun get(id: String): String? {
        return if (id == "rtt") {
            rtt.toString()
        } else if (id.startsWith("request.") || id.startsWith("response.")) {
            val side = if (id.startsWith("req")) request else response
            side?.get(id.substring(id.indexOf('.') + 1))
        } else {
            val value = request?.get(id)
            return value ?: response?.get(id)
        }
    }

    public val rtt: Long
        get() = if (request != null && response != null) response!!.timestamp.getMillis() - request!!.timestamp.getMillis() else -1

    public val mti: String
        get() = request?.get("0") ?: ""

    public fun toFields(ids: List<String>): List<Pair<String, String?>> {
        return ids.map { id -> Pair(id, this[id]) }
    }

    public fun toFieldValues(ids: List<String>): List<String?> {
        return ids.map { id -> this[id] }
    }
}

public fun List<EntryPair>.toCsv(vararg ids: String): String = toCsv(listOf(*ids))

public fun List<EntryPair>.toCsv(ids: List<String>): String {
    return toCsvRows(ids).map { it.toCsv() }.join("\n")
}

public fun List<Pair<String, String?>>.toCsv(): String {
    return map { p -> p.second }.joinToString(",")
}

public fun List<EntryPair>.toCsvRows(ids: List<String>): List<List<Pair<String, String?>>> {
    return map { it.toFields(ids) }
}

public fun List<Pair<String, String?>>.valuesOf(ids: List<String>): List<String> {
    var keyFields = ArrayList<String>()
    forEach { p ->
        if (ids.contains(p.first))
            keyFields.add(p.second)
    }
    return keyFields
}

/**
 * Slightly odd. Returned list elements are EntryPair or Group
 */
public fun List<EntryPair>.coalesceBy(selector: (EntryPair) -> String): List<Any> {
    var group = ArrayList<EntryPair>()
    var groupKey = ""
    val coalesced = ArrayList<Any>()

    fun addEntry() {
        if (group.notEmpty) {
            coalesced.add(group.first)
            if (group.size > 1) {
                coalesced.add(Group(group.size - 1, groupKey))
            }
        }
    }

    forEach { line ->
        val key = selector(line)
        if (key != groupKey) {
            addEntry()
            group.clear()
        }
        group.add(line)
        groupKey = key
    }
    addEntry()
    return coalesced
}

public data class Group(public val size: Int, public val key: String)