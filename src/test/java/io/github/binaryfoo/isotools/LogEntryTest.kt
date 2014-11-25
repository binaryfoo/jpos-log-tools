package io.github.binaryfoo.isotools

import org.jetbrains.spek.api.Spek

class LogEntryTest: Spek() {{

    given("a single line of a log record") {
        on("reading") {
            it("should extract id and value") {
                val attributes = extractAttributes("""<field id="7" value="1124000003"/>""")
                test.assertEquals("7", attributes["id"])
                test.assertEquals("1124000003", attributes["value"])
            }
        }
    }

    given("a whole a log record") {
        val timestamp = "Mon Nov 24 00:00:03 EST 2014.292"
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
                test.assertEquals("1124000003", entry["7"])
                test.assertEquals("28928", entry["11"])
                test.assertEquals("a subfield", entry["48.1"])
                test.assertEquals("subfield 48.2.13", entry["48.2.13"])
            }

            it("should extract the timestamp") {
                test.assertEquals(timestamp, entry.at)
            }

            it("should extract the realm") {
                test.assertEquals(realm, entry.realm)
            }
        }
    }
}}