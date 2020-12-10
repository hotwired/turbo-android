package dev.hotwire.turbo.visit

import dev.hotwire.turbo.util.toObject
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

data class TurboVisitOptions(
    @SerializedName("action") val action: TurboVisitAction = TurboVisitAction.ADVANCE,
    @SerializedName("snapshotHTML") val snapshotHTML: String? = null,
    @SerializedName("response") val response: TurboVisitResponse? = null
) {
    companion object {
        fun fromJSON(json: String?): TurboVisitOptions? = try {
            json?.toObject(object : TypeToken<TurboVisitOptions>() {})
        } catch (e: Exception) {
            null
        }
    }
}
