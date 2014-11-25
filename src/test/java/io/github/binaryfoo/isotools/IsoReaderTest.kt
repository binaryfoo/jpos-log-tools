package io.github.binaryfoo.isotools

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals
import java.io.File

class IsoReaderTest: Spek() {{

    given("a log file with one entry") {
        val reader = IsoReader()
        on("reading the log") {
            val entries = reader.read(File("src/test/resources/samples/basic.txt"))
            it("should find two entries") {
                test.assertEquals(2, entries.size)
            }
            it("should read the fields from an isomsg") {
                val entry = entries[0]
                assertEquals("0800", entry["0"])
                assertEquals("1124000003", entry["7"])
                assertEquals("28928", entry["11"])
            }
            it("should read the header fields from the log entry") {
                val entry = entries[0]
                assertEquals("Mon Nov 24 00:00:03 EST 2014.292", entry.at)
                assertEquals("some.channel/10.0.0.1:4321", entry.realm)
            }
            it("should handle a log rotation entry") {
                assertEquals("rotate-log-listener", entries[1].realm)
            }
        }
    }

    given("a log file with a few entries") {
        val reader = IsoReader()
        on("reading the log") {
            val entries = reader.read(File("src/test/resources/samples/handful.txt"))
            val csv = entries.pairRequestWithResponse().toCsv("0", "11", "request.at", "response.at")
            assertEquals("0800,28928,Mon Nov 24 00:00:03 EST 2014.292,Mon Nov 24 00:00:04 EST 2014.100", csv)
        }
    }
}}