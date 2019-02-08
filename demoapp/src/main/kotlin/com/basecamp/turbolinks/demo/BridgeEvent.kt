package com.basecamp.turbolinks.demo

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

data class BridgeEvent(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("data") val data: String) {

    fun toJSON(): String {
        return Gson().toJson(this)
    }

    companion object {
        fun fromJSON(json: String?): BridgeEvent? {
            return try {
                val type = object : TypeToken<BridgeEvent>() {}.type
                Gson().fromJson<BridgeEvent>(json, type)
            } catch (e: Exception) {
                null
            }
        }
    }
}
