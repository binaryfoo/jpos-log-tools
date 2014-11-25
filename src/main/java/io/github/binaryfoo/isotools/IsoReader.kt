package io.github.binaryfoo.isotools

import java.io.File
import java.util.ArrayList

public class IsoReader {

    public fun read(f: File): List<LogEntry> {
        var entries: MutableList<List<String>> = ArrayList()
        var inRecord = false
        var record: MutableList<String> = ArrayList()
        f.readLines().forEach { line ->
            if (line.startsWith("<log ")) {
                inRecord = true
            }
            if (inRecord) {
                record.add(line)
            }
            if (line.startsWith("</log>")) {
                entries.add(record)
                record = ArrayList()
                inRecord = false
            }
        }
        return entries.map {
            fromLines(it)
        }
    }
}