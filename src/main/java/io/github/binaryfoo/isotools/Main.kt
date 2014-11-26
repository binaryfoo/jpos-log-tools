package io.github.binaryfoo.isotools

import java.util.ArrayList
import java.io.File

public fun main(args: Array<String>) {
    if (args.size < 1) {
        println("arguments: <field list> [<file name> ...]")
        return
    }
    val entries: List<LogEntry> = IsoReader().readStdinOrFiles(args)
    val csv = entries.pairRequestWithResponse()
            .toCsv(args.first().split(',').toList())
    print(csv)
}
