package com.hotwire.turbo.visit

import com.google.gson.annotations.SerializedName

data class TurbolinksVisitResponse(
    @SerializedName("statusCode") val statusCode: Int,
    @SerializedName("responseHTML") val responseHTML: String? = null
)
