package com.tsundoku.anilist.viewer

import com.tsundoku.AddTsundokuListMutation
import com.tsundoku.GetCustomListsQuery
import com.tsundoku.UpdateMediaNotesMutation
import com.tsundoku.ViewerQuery
import com.tsundoku.models.Media
import com.tsundoku.models.Viewer
import kotlinx.coroutines.flow.Flow

interface ViewerRepository {
    fun getAniListViewer(): Flow<Result<ViewerQuery.Viewer>>
    fun getCustomLists(userId: Int): Flow<Result<GetCustomListsQuery.MediaList>>
    fun addTsundokuList(customLists: MutableList<String>): Flow<Result<AddTsundokuListMutation.UpdateUser>>
    fun updateAniListMediaNotes(mediaId: Int, newNote: String): Flow<Result<UpdateMediaNotesMutation.SaveMediaListEntry>>
    suspend fun getDatabaseViewerCurrencyCode(viewerId: Int): Viewer?
    suspend fun insertDatabaseViewer(viewerId: Int)
    suspend fun insertNewMedia(mediaList: List<Media>)
    suspend fun getMediaList(viewerId: Int): List<Media>
    suspend fun updateMedia(viewerId: Int, mediaId: String, updateMap: Map<String, Any?>)
}