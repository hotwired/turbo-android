package dev.hotwire.turbo.visit

import com.google.gson.annotations.SerializedName
import dev.hotwire.turbo.util.truncateMiddle
import dev.hotwire.turbo.util.withoutNewLineChars
import dev.hotwire.turbo.util.withoutRepeatingWhitespace

data class TurboVisitResponse(
    @SerializedName("statusCode") val statusCode: Int,
    @SerializedName("responseHTML") val responseHTML: String? = null
) {
    override fun toString(): String {
        val response = responseHTML
            ?.withoutNewLineChars()
            ?.withoutRepeatingWhitespace()
            ?.truncateMiddle(maxChars = 50)

        return "TurboVisitResponse(" +
                    "statusCode=$statusCode, " +
                    "responseHTML=$response, " +
                    "responseLength=${responseHTML?.length ?: 0}" +
                ")"
    }
}
