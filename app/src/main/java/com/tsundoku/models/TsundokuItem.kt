package com.tsundoku.models

data class TsundokuItem(
    val media: Media,
    val website: Website,
    val title: String,
    val countryOfOrigin: String,
    val format: String,
    val chapters: Int,
    var notes: String,
    val imageUrl: String
)
