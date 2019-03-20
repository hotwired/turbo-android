package com.basecamp.turbolinks

import androidx.lifecycle.ViewModel

class TurbolinksSharedViewModel : ViewModel() {
    // Modal result can only be retrieved once
    var modalResult: TurbolinksModalResult? = null
        get() = field.also { field = null }
}

data class TurbolinksModalResult(
    val location: String,
    val action: String
)
