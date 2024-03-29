package com.tsundoku.models

import androidx.compose.runtime.MutableState
import java.math.BigDecimal

data class TsundokuItem(
    val mediaId: String,
    val website: Website,
    val title: String,
    val countryOfOrigin: String,
    val status: String,
    val format: String,
    val chapters: Int,
    var notes: String,
    val imageUrl: String,
    var cost: BigDecimal,
    var curVolumes: MutableState<String>,
    var maxVolumes: MutableState<String>,
)
