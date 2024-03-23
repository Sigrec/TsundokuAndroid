package com.tsundoku.ui.model

class MediaModel {
    companion object {
        fun getCorrectFormat(format: String, countryOfOrigin: String): String {
            return if (format == "MANGA") {
                when (countryOfOrigin) {
                    "KR", "ko" -> "Manhwa"
                    "CN", "TW", "zh", "zh-hk" -> "Manhua"
                    "FR", "fr" -> "Manfra"
                    "EN", "en" -> "Comic"
                    else -> "Manga"
                }
            } else "Novel"
        }
        val costRegex = Regex("^\\d+[.]?(?:[\\d]{1,2})?\$")
    }
}