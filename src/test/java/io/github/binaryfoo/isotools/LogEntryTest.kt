package io.github.binaryfoo.isotools

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals
import org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.Matchers.hasItem;
import org.joda.time.DateTime

class LogEntryTest: Spek() {{

    given("a single line of a log record") {
        on("reading") {
            it("should extract id and value") {
                val attributes = extractAttributes("""<field id="7" value="1124000003"/>""")
                assertEquals("7", attributes["id"])
                assertEquals("1124000003", attributes["value"])
            }
        }
    }

    given("a whole a log record") {
        val timestamp = "Mon Nov 24 16:59:03 EST 2014.292"
        val realm = "some.channel/10.0.0.1:4321"
        val record = """<log realm="$realm" at="$timestamp" lifespan="10005ms">
  <receive>
    <isomsg direction="incoming">
      <!-- org.jpos.iso.packager.XMLPackager -->
      <field id="0" value="0800"/>
      <field id="7" value="1124000003"/>
      <field id="11" value="28928"/>
      <isomsg id="48">
        <field id="1" value="a subfield"/>
        <isomsg id="2">
          <field id="13" value="subfield 48.2.13"/>
        </isomsg>
      </isomsg>
    </isomsg>
  </receive>
</log>"""
        on("reading") {
            val entry = fromLines(record.split('\n'))
            it("should extract the fields") {
                assertEquals("1124000003", entry["7"])
                assertEquals("28928", entry["11"])
                assertEquals("a subfield", entry["48.1"])
                assertEquals("subfield 48.2.13", entry["48.2.13"])
            }

            it("should extract the 'at' attribtue") {
                assertEquals(timestamp, entry.at)
            }

            it("parse the 'at' attribtue into a timestamp") {
                assertEquals(DateTime(2014, 11, 24, 16, 59, 3, 292), entry.timestamp)
            }

            it("should extract the timestamp") {
                assertEquals(timestamp, entry.at)
            }

            it("should extract the realm") {
                assertEquals(realm, entry.realm)
            }

            it("root attributes are accessible using []") {
                assertEquals(timestamp, entry["at"])
                assertEquals(realm, entry["realm"])
            }
        }
    }

    given("a list of log entries") {
        val entry1 = entry("11" to "123456", "2" to "pan1", "37" to "123456001", "41" to "1")
        val entry2 = entry("11" to "123457", "2" to "pan1", "37" to "123456002", "41" to "2")
        val entry3 = entry("11" to "123458", "2" to "pan1", "37" to "123456003", "41" to "1")
        val list = listOf(
                entry1,
                entry2,
                entry3
        )
        on("filtering") {
            it("matches by a single field") {
                val filtered = list.filter(setOf("11" to "123456"))
                assertEquals(listOf(entry1), filtered)
            }
            it("matches by two fields") {
                val filtered = list.filter(setOf("2" to "pan1", "41" to "1"))
                assertEquals(listOf(entry1, entry3), filtered)
            }
        }
    }

    given("a list of intermingled (request, response) pairs") {
        val auth1 = entry("0" to "0200", "11" to "1")
        val auth1Response = entry("0" to "0210", "11" to "1", "39" to "00")
        val list = listOf(
                auth1,
                entry("0" to "0200", "11" to "2"),
                entry("0" to "0820", "11" to "3"),
                auth1Response,
                entry("0" to "0820", "11" to "4"),
                entry("0" to "0830", "11" to "3"),
                entry("0" to "0210", "11" to "2", "39" to "01"),
                entry("0" to "0200", "11" to "5")
        )
        on("matching responses to requests") {
            val pairing = list.pairRequestWithResponse()
            it("matches a 0210 to a 0200") {
                assertThat(pairing, hasItem(EntryPair(auth1, auth1Response)))
            }
            it("can be flattened to .csv output") {
                val csv = pairing.toCsv("0", "11", "39")
                assertEquals("""0200,1,00
0820,3,null
0200,2,01""", csv)
            }
        }
    }

    given("a (request, response) pair") {
        val reversal1 = entry("""<log at="Mon Nov 17 00:00:03 EST 2014.292">
    <isomsg>
        <field id="0" value="0420"/>
        <field id="11" value="131415"/>
    </isomsg>
</log>""")
        val reversal1Response = entry("""<log at="Mon Nov 17 00:00:04 EST 2014.301">
    <isomsg>
        <field id="0" value="0430"/>
        <field id="11" value="131415"/>
    </isomsg>
</log>""")
        val list = listOf(reversal1, reversal1Response)
        on("matching") {
            val pairing = list.pairRequestWithResponse()
            it("can determine the round trip time") {
                assertEquals(1009L, pairing[0].rtt)
            }
        }
    }

}
    private fun entry(vararg values: Pair<String, String>) = LogEntry(mapOf(*values))

    private fun entry(e: String) = fromLines(e.split('\n'))
}


