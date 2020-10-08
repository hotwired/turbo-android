package com.basecamp.turbolinks.visit

import android.annotation.SuppressLint
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

@SuppressLint("DefaultLocale")
class TurbolinksVisitActionAdapter : TypeAdapter<TurbolinksVisitAction>() {
    override fun read(reader: JsonReader): TurbolinksVisitAction {
        return try {
            TurbolinksVisitAction.valueOf(reader.nextString().toUpperCase())
        } catch (e: IllegalArgumentException) {
            TurbolinksVisitAction.ADVANCE
        }
    }

    override fun write(writer: JsonWriter, action: TurbolinksVisitAction) {
        writer.value(action.name.toLowerCase())
    }
}
