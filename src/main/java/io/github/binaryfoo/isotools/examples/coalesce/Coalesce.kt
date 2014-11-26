package io.github.binaryfoo.isotools.examples.coalesce

import io.github.binaryfoo.isotools.IsoReader
import io.github.binaryfoo.isotools.LogEntry
import java.io.File
import io.github.binaryfoo.isotools.pairRequestWithResponse
import io.github.binaryfoo.isotools.coalesceBy
import io.github.binaryfoo.isotools.EntryPair
import io.github.binaryfoo.isotools.toCsv
import io.github.binaryfoo.isotools.Group
import kotlin.platform.platformStatic

public fun main(args: Array<String>) {
    if (args.size < 1) {
        println("arguments: [<file name> ...]")
        return
    }
    val entries: List<LogEntry> = IsoReader().readStdinOrFiles(args)
    val fields = "0,70,11,53,39,request.time,rtt".split(',').toList()
    val csv: List<Any> = entries.pairRequestWithResponse()
            .filter{ it.mti == "0200" }
            .coalesceBy{ e ->
                val normalized39 = if (e["39"] == "01") e["39"] else "ok"
                "KS " + e["53"] + " with " + normalized39
            }
    csv.forEach {
        val s = when (it) {
            is EntryPair -> {
                it.toFields(fields).toCsv()
            }
            is Group -> {
                "... ${it.size} with ${it.key} ..."
            }
            else -> {
                ""
            }
        }
        println(s)
    }
}