package io.github.binaryfoo.isotools

import java.util.ArrayList
import org.joda.time.DateTime
import java.util.LinkedHashMap

/**
 * A (request,response) pair of messages from a jpos log.
 */
public data class EntryPair(public val request: LogEntry?, public val response: LogEntry?) : CoalesceResult {

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

    public val timestamp: DateTime
        get() = request!!.timestamp

    public fun toFields(vararg ids: String): LooseEntry = toFields(listOf(*ids))

    public fun toFields(ids: List<String>): LooseEntry {
        val m = LooseEntry()
        ids.forEach { m[it] = this[it] }
        return m
    }

}

/**
 * Map containing a subset of (id,value) pairs from a single EntryPair.
 *
 * Eg the stan, time, round trip time and response code for an (auth, response) pair.
 */
public class LooseEntry : LinkedHashMap<String, String>() {
    public fun toCsv(): String = values().join(",")
}

public fun List<EntryPair>.toCsv(vararg ids: String): String = toCsv(listOf(*ids))

public fun List<EntryPair>.toCsv(ids: List<String>): String {
    return toCsvRows(ids).toCsv()
}

public fun List<LooseEntry>.toCsv(): String {
    return map { it.toCsv() }.join("\n")
}

public fun List<EntryPair>.toCsvRows(vararg ids: String): List<LooseEntry> = toCsvRows(listOf(*ids))

public fun List<EntryPair>.toCsvRows(ids: List<String>): List<LooseEntry> {
    return map { it.toFields(ids) }
}

/**
 * Slightly odd. Returned list elements are EntryPair or Group
 */
public fun List<EntryPair>.coalesceBy(selector: (EntryPair) -> String): List<CoalesceResult> {
    var group = ArrayList<EntryPair>()
    var groupKey = ""
    val coalesced = ArrayList<CoalesceResult>()

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

public trait CoalesceResult

public data class Group(public val size: Int, public val key: String) : CoalesceResult