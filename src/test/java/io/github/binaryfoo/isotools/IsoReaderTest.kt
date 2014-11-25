package io.github.binaryfoo.isotools

import org.jetbrains.spek.api.Spek
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
                test.assertEquals("0800", entry["0"])
                test.assertEquals("1124000003", entry["7"])
                test.assertEquals("28928", entry["11"])
            }
            it("should handle a log rotation entry") {
                test.assertEquals("rotate-log-listener", entries[1].realm)
            }
        }
    }
}}