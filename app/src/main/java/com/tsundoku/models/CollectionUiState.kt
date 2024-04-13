package com.tsundoku.models

import com.tsundoku.UserQuery

data class CollectionUiState(
    val onViewer: Boolean = true,
    val successfulUserSearch: Boolean = false,
    val curSearchUser: UserQuery.User? = null,
    val curEditingMediaIndex: Int = -1
)
