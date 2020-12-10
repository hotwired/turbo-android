package dev.hotwire.turbo.visit

import dev.hotwire.turbo.util.toObject
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

data class TurbolinksVisitOptions(
    @SerializedName("action") val action: TurbolinksVisitAction = TurbolinksVisitAction.ADVANCE,
    @SerializedName("snapshotHTML") val snapshotHTML: String? = null,
    @SerializedName("response") val response: TurbolinksVisitResponse? = null
) {
    companion object {
        fun fromJSON(json: String?): TurbolinksVisitOptions? = try {
            json?.toObject(object : TypeToken<TurbolinksVisitOptions>() {})
        } catch (e: Exception) {
            null
        }
    }
}
