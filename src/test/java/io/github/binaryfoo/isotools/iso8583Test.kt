package io.github.binaryfoo.isotools

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

class iso8583Test: Spek() {{
    given("the iso 8583 domain") {
        on("normalising an MTI") {
            it("maps responses back to request") {
                assertEquals("0200", normaliseToRequestMTI("0210"))
                assertEquals("0800", normaliseToRequestMTI("0810"))
                assertEquals("0820", normaliseToRequestMTI("0830"))
                assertEquals("2220", normaliseToRequestMTI("2230"))
                assertEquals("0420", normaliseToRequestMTI("0430"))
            }
            it("leaves requests alone") {
                assertEquals("0200", normaliseToRequestMTI("0200"))
                assertEquals("0220", normaliseToRequestMTI("0220"))
            }
            it("ignores rubbish data") {
                assertEquals("", normaliseToRequestMTI(""))
                assertEquals("020", normaliseToRequestMTI("020"))
                assertEquals("02300", normaliseToRequestMTI("02300"))
            }
        }
    }
}}
