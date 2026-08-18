package dev.hotwire.turbo.http

import android.webkit.CookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

internal class WebViewCookieJar : CookieJar {
    private val cookieManager = CookieManager.getInstance()

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val header = cookieManager.getCookie(url.toString()) ?: return emptyList()
        return header.split("; ").mapNotNull { Cookie.parse(url, it) }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.forEach { cookieManager.setCookie(url.toString(), it.toString()) }
    }
}
