package io.github.binaryfoo.isotools.examples

import io.github.binaryfoo.isotools.IsoReader
import io.github.binaryfoo.isotools.LogEntry
import java.io.File
import io.github.binaryfoo.isotools.pairRequestWithResponse
import io.github.binaryfoo.isotools.coalesceBy
import io.github.binaryfoo.isotools.EntryPair
import io.github.binaryfoo.isotools.toCsv
import io.github.binaryfoo.isotools.Group
import kotlin.platform.platformStatic

class Coalesce {
    class object {

        public fun main(args: Array<String>): Unit {
            if (args.size < 1) {
                println("arguments: <field list> [<file name> ...]")
                return
            }
            val reader = IsoReader()
            val entries: List<LogEntry> = if (args.size == 1) {
                reader.read(System.`in`)
            } else {
                reader.readAll(args.toList().subList(1, args.lastIndex + 1).map { File(it) })
            }
            val fields = args.first().split(',').toList()
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
    }
}

public fun main(args: Array<String>): Unit = Coalesce.main(args)