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

    given("a log file with two entries") {
        val reader = IsoReader()
        on("reading the log") {
            val entries = reader.read(File("src/test/resources/samples/a-pair.txt"))
            val pairing = entries.pairRequestWithResponse()
            it("can be flattened into a .csv file with request and response") {
                val csv = pairing.toCsv("0", "11", "request.at", "response.at")
                assertEquals("0800,28928,Mon Nov 24 00:00:03 EST 2014.292,Mon Nov 24 00:00:04 EST 2014.100", csv)
            }
            it("can include full timestamps and round trip time in .csv") {
                val csv = pairing.toCsv("0", "11", "request.timestamp", "response.timestamp", "rtt")
                assertEquals("0800,28928,2014-11-24 00:00:03.292,2014-11-24 00:00:04.100,808", csv)
            }
            it("can include times only in .csv") {
                val csv = pairing.toCsv("0", "request.time", "response.time", "realm")
                assertEquals("0800,00:00:03.292,00:00:04.100,some.channel/10.0.0.1:4321", csv)
            }
        }
    }
}}