package com.nagutos.nyaaandroid.model

/**
 * The two sibling nyaa indexes the app can browse. Sukebei is the adult (18+) instance and
 * has its own category taxonomy; everything else (list/detail HTML layout) is identical, so
 * a single [baseUrl] switch drives the whole app.
 *
 * [baseUrl] has no trailing slash so it concatenates cleanly with relative paths ("/view/...").
 */
enum class NyaaSite(val baseUrl: String) {
    NYAA("https://nyaa.si"),
    SUKEBEI("https://sukebei.nyaa.si")
}
