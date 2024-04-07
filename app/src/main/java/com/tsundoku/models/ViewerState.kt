package com.tsundoku.models

import com.tsundoku.TSUNDOKU_DEFAULT_CURRENCY_CODE
import com.tsundoku.TSUNDOKU_DEFAULT_CURRENCY_SYMBOL
import com.tsundoku.ViewerQuery
import com.tsundoku.anilist.enums.Lang
import java.math.BigDecimal
import java.math.RoundingMode

data class ViewerState(
    var viewer: ViewerQuery.Viewer? = null,
    var viewerId: Int = -1,
    var currencyCode: String = TSUNDOKU_DEFAULT_CURRENCY_CODE,
    var currencySymbol: String = TSUNDOKU_DEFAULT_CURRENCY_SYMBOL,
    var preferredLang: Lang = Lang.ROMAJI,
    var collectionCost: BigDecimal = BigDecimal(0.00).setScale(2, RoundingMode.HALF_UP),
    var seriesCount: Int = 0,
    var chapters: Int = 0,
    var volumes: Int = 0,
    var cancelledCount: Float = 0f,
    var hiatusCount: Float = 0f,
    var ongoingCount: Float = 0f,
    var finishedCount: Float = 0f,
    var comingSoonCount: Float = 0f,
)
