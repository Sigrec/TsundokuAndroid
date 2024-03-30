package com.tsundoku.models

import androidx.compose.runtime.MutableState
import com.tsundoku.data.TsundokuFormat
import com.tsundoku.data.TsundokuStatus
import java.math.BigDecimal

data class TsundokuItem(
    val mediaId: String,
    val website: Website,
    val title: String,
    val countryOfOrigin: String,
    val status: TsundokuStatus,
    val format: TsundokuFormat,
    val chapters: Int,
    var notes: String,
    val imageUrl: String,
    var cost: BigDecimal,
    var curVolumes: MutableState<String>,
    var maxVolumes: MutableState<String>,
)
