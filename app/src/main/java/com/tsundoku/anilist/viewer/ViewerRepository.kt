package com.tsundoku.anilist.viewer

import com.tsundoku.AddMediaToTsundokuMutation
import com.tsundoku.AddTsundokuListMutation
import com.tsundoku.DeleteMediaFromTsundokuMutation
import com.tsundoku.GetCustomListsQuery
import com.tsundoku.GetMediaCustomListsQuery
import com.tsundoku.GetMediaEntryQuery
import com.tsundoku.UpdateMediaNotesMutation
import com.tsundoku.ViewerQuery
import com.tsundoku.data.TsundokuFormat
import com.tsundoku.models.Media
import com.tsundoku.models.Viewer
import com.tsundoku.models.VolumeUpdateMedia
import kotlinx.coroutines.flow.Flow

interface ViewerRepository {
    fun getAniListViewer(): Flow<Result<ViewerQuery.Viewer>>
    fun getViewerCustomLists(userId: Int): Flow<Result<GetCustomListsQuery.MediaList>>
    fun addTsundokuList(customLists: MutableList<String>): Flow<Result<AddTsundokuListMutation.UpdateUser>>
    fun updateAniListMediaNotes(mediaId: Int, newNote: String): Flow<Result<UpdateMediaNotesMutation.SaveMediaListEntry>>
    fun addAniListMediaToCollection(mediaId: Int, customLists: List<String>): Flow<Result<AddMediaToTsundokuMutation.SaveMediaListEntry>>
    fun deleteAniListMediaFromCollection(mediaId: Int, customLists: List<String>): Flow<Result<DeleteMediaFromTsundokuMutation.SaveMediaListEntry>>
    fun getMediaCustomLists(viewerId: Int, mediaId: Int): Flow<Result<GetMediaCustomListsQuery.MediaList>>
    fun getNewAniListMediaSeries(
        seriesId: Int?,
        title: String?,
        format: TsundokuFormat
    ): Flow<Result<GetMediaEntryQuery.Media>>
    suspend fun getDatabaseViewerCurrencyCode(viewerId: Int): Viewer?
    suspend fun insertDatabaseViewer(viewerId: Int)
    suspend fun insertNewMedia(mediaList: List<Media>)
    suspend fun updateCurrencyCode(viewerId: Int, currencyCode: String)
    suspend fun getMediaList(viewerId: Int): List<Media>
    suspend fun updateMedia(viewerId: Int, mediaId: String, updateMap: Map<String, Any?>)
    suspend fun deleteMedia(viewerId: Int, deleteList: List<String>)
    suspend fun batchCurVolumesUpdateMedia(viewerId: Int, updateList: MutableList<VolumeUpdateMedia>)
}