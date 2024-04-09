package com.tsundoku.extensions

import android.webkit.CookieManager
import com.tsundoku.ANILIST_URL
import okhttp3.HttpUrl.Companion.toHttpUrl


object CookiesExt {
    fun removeCookie() {
        val cookieManager = CookieManager.getInstance()
        val url = ANILIST_URL
        val cookies = cookieManager.getCookie(url) ?: return

        val paths = url.toHttpUrl().encodedPathSegments.scan("/") { prefix, segment -> "$prefix$segment/" }
        val keys = cookies.split(";")
            .map { it.substringBefore("=", missingDelimiterValue = "").trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        keys.forEach { key ->
            paths.forEach {
                // Max-Age effectively removes the cookie
                cookieManager.setCookie(url, "$key=; Path=$it; Max-Age=-1")
            }
        }
    }
}