package com.tsundoku.models

import com.tsundoku.TSUNDOKU_DEFAULT_CURRENCY_CODE
import com.tsundoku.TSUNDOKU_DEFAULT_CURRENCY_SYMBOL

data class ViewerState(
    var viewerId: Int = -1,
    var currencyCode: String = TSUNDOKU_DEFAULT_CURRENCY_CODE,
    var currencySymbol: String = TSUNDOKU_DEFAULT_CURRENCY_SYMBOL,
    var collection: MutableList<TsundokuItem>? = mutableListOf()
)
