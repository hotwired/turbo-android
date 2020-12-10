package dev.hotwire.turbo.visit

import com.google.gson.annotations.SerializedName

data class TurboVisitResponse(
    @SerializedName("statusCode") val statusCode: Int,
    @SerializedName("responseHTML") val responseHTML: String? = null
)
