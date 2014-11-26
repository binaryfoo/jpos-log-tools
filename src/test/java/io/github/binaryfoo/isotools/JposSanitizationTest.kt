package io.github.binaryfoo.isotools

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals
import org.joda.time.DateTime

class JposSanitizationTest: Spek() {{
    given("The creative date format used in jpos logs") {
        on("attempting to parse") {
            it("makes some headway") {
                assertEquals(DateTime(2014, 11, 24, 16, 59, 3, 292), parseTimestamp("Mon Nov 24 16:59:03 EST 2014.292"))
            }
            it("handles 2 digit millis") {
                assertEquals(DateTime(2014, 11, 23, 0, 59, 3, 29), parseTimestamp("Sun Nov 23 00:59:03 EST 2014.29"))
            }
            it("handles 1 digit millis") {
                assertEquals(DateTime(2014, 11, 25, 23, 0, 0, 2), parseTimestamp("Tue Nov 25 23:00:00 EST 2014.2"))
            }
        }
    }
}}