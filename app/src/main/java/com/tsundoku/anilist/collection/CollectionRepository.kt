package com.tsundoku.anilist.collection

import com.tsundoku.GetTsundokuCollectionQuery
import com.tsundoku.UserQuery
import com.tsundoku.ViewerQuery
import com.tsundoku.type.MediaListSort
import kotlinx.coroutines.flow.Flow

interface CollectionRepository {
    fun getSearchedUser(username: String): Flow<Result<UserQuery.User>>
    // fun getCustomLists(userId: Int): Flow<Result<GetCustomListsQuery.MediaList>>
    fun getTsundokuCollection(username: String?, userId: Int?, titleSort: List<MediaListSort?>): Flow<Result<List<GetTsundokuCollectionQuery.List?>>>
}