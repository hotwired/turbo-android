package com.basecamp.turbolinks

import com.google.gson.annotations.SerializedName

data class VisitOptions(
    @SerializedName("action") val action: String = TurbolinksSession.ACTION_ADVANCE,
    @SerializedName("response") val response: VisitResponse? = null
)

data class VisitResponse(
    @SerializedName("statusCode") val statusCode: Int,
    @SerializedName("responseHTML") val responseHTML: String? = null
)
