package dev.hotwire.turbo.visit

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TurboVisitResponseTest {
    @Test
    fun toStringWithNoResponse() {
        val response = TurboVisitResponse(
            statusCode = 200,
            responseHTML = null
        )

        assertThat(response.toString()).isEqualTo(
            "TurboVisitResponse(statusCode=200, responseHTML=null, responseLength=0)"
        )
    }

    @Test
    fun toStringWithShortResponse() {
        val response = TurboVisitResponse(
            statusCode = 200,
            responseHTML = "<html><head></head></html>"
        )

        assertThat(response.toString()).isEqualTo(
            "TurboVisitResponse(statusCode=200, responseHTML=<html><head></head></html>, responseLength=26)"
        )
    }

    @Test
    fun toStringWithTruncatedResponse() {
        val response = TurboVisitResponse(
            statusCode = 200,
            responseHTML = "<html><head></head>This is a really long response that is truncated.</html>"
        )

        assertThat(response.toString()).isEqualTo(
            "TurboVisitResponse(" +
                "statusCode=200, " +
                "responseHTML=<html><head></head>This i [...] that is truncated.</html>, " +
                "responseLength=75" +
            ")"
        )
    }

    @Test
    fun toStringWithTruncatedResponseAndWhitespace() {
        val response = TurboVisitResponse(
            statusCode = 200,
            responseHTML = "<html>\n<head></head>This   is a really long response that is truncated.\n</html>"
        )

        assertThat(response.toString()).isEqualTo(
            "TurboVisitResponse(" +
                    "statusCode=200, " +
                    "responseHTML=<html><head></head>This i [...] that is truncated.</html>, " +
                    "responseLength=79" +
                    ")"
        )
    }
}
