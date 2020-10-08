package com.basecamp.turbolinks.demo.util

class Error {
    companion object {
        fun getMessage(statusCode: Int): String {
            return when (statusCode) {
                404 -> "Oops! Page could not be found."
                else -> "Oops! An error occurred."
            }
        }
    }
}
