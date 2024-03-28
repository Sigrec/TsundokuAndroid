package com.tsundoku.models

import com.tsundoku.TSUNDOKU_DEFAULT_CURRENCY_CODE
import com.tsundoku.TSUNDOKU_DEFAULT_CURRENCY_SYMBOL
import com.tsundoku.anilist.enums.Lang
import java.math.BigDecimal

data class ViewerState(
    var viewerId: Int = -1,
    var currencyCode: String = TSUNDOKU_DEFAULT_CURRENCY_CODE,
    var currencySymbol: String = TSUNDOKU_DEFAULT_CURRENCY_SYMBOL,
    var selectedPaneIndex: Int = 0,
    var preferredLang: Lang = Lang.ROMAJI,
    var collectionCost: BigDecimal = BigDecimal(0.00)
)
