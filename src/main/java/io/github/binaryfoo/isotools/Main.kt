package io.github.binaryfoo.isotools

import java.util.ArrayList
import java.io.File

public fun main(args: Array<String>) {
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
    val csv = entries.pairRequestWithResponse().filter { it.mti == "0200" && it.rtt > 500 }.toCsv(args.first().split(',').toList())
    print(csv)
}
