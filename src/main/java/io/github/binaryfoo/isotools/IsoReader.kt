package io.github.binaryfoo.isotools

import java.io.File
import java.util.ArrayList
import java.io.BufferedReader
import java.io.FileReader
import java.io.InputStream
import java.io.FileInputStream

public class IsoReader {

    public fun read(f: File): List<LogEntry> {
        return FileInputStream(f).use {
            read(it)
        }
    }

    public fun readStdinOrFiles(args: Array<String>, first: Int = 1): List<LogEntry> {
        return if (args.size == first) {
            read(System.`in`)
        } else {
            readAll(args.toList().subList(first, args.lastIndex + first).map { File(it) })
        }
    }

    public fun read(input: InputStream): List<LogEntry> {
        var entries: MutableList<List<String>> = ArrayList()
        var inRecord = false
        var record: MutableList<String> = ArrayList()
        for (line in BufferedReader(input.reader()).lines()) {
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

    public fun readAll(files: List<File>): List<LogEntry> = files.flatMap { read(it) }
}