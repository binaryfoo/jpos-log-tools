package io.github.binaryfoo.isotools

public data class EntryPair(public val request: LogEntry?, public val response: LogEntry?) {

    public fun get(id: String): String? {
        return if (id == "rtt") {
            rtt?.toString()
        } else if (id.startsWith("request.") || id.startsWith("response.")) {
            val side = if (id.startsWith("req")) request else response
            side?.get(id.substring(id.indexOf('.') + 1))
        } else {
            val value = request?.get(id)
            return value ?: response?.get(id)
        }
    }

    public val rtt: Long?
    get() = if (request != null && response != null) response!!.timestamp.getMillis() - request!!.timestamp.getMillis() else null
}

public fun toFields(vararg ids: String): (EntryPair) -> List<Pair<String, String?>> {
    return { p ->
        ids.map { id -> Pair(id, p[id]) }
    }
}

public fun toFieldValues(vararg ids: String): (EntryPair) -> List<String?> {
    return { p ->
        ids.map { id -> p[id] }
    }
}

public fun List<EntryPair>.toCsv(vararg ids: String): String {
    val reducedPairs: List<List<String?>> = this.map(toFieldValues(*ids))
    return reducedPairs.map { it.joinToString(",") }.join("\n")
}