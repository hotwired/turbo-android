package com.basecamp.turbolinks.core

import android.annotation.SuppressLint
import com.basecamp.turbolinks.core.VisitAction.ADVANCE
import com.basecamp.turbolinks.core.VisitAction.valueOf
import com.basecamp.turbolinks.util.toObject
import com.google.gson.TypeAdapter
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

data class VisitOptions(
    @SerializedName("action") val action: VisitAction = ADVANCE,
    @SerializedName("snapshotHTML") val snapshotHTML: String? = null,
    @SerializedName("response") val response: VisitResponse? = null
) {
    companion object {
        fun fromJSON(json: String?): VisitOptions? = try {
            json?.toObject(object : TypeToken<VisitOptions>() {})
        } catch (e: Exception) {
            null
        }
    }
}

data class VisitResponse(
    @SerializedName("statusCode") val statusCode: Int,
    @SerializedName("responseHTML") val responseHTML: String? = null
)

enum class VisitAction {
    ADVANCE,
    REPLACE,
    RESTORE
}

@SuppressLint("DefaultLocale")
class VisitActionAdapter : TypeAdapter<VisitAction>() {
    override fun read(reader: JsonReader): VisitAction {
        return try {
            valueOf(reader.nextString().toUpperCase())
        } catch (e: IllegalArgumentException) {
            ADVANCE
        }
    }

    override fun write(writer: JsonWriter, action: VisitAction) {
        writer.value(action.name.toLowerCase())
    }
}
