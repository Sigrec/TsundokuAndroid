package com.tsundoku.ui.model

data class CollectionUiState(
    val onViewer: Boolean = true,
    val curUser: String = "",
    val curEditingMediaIndex: Int = -1
)
