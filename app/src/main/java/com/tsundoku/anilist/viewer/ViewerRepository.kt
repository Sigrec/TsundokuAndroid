package com.tsundoku.anilist.viewer

import com.tsundoku.AddTsundokuListMutation
import com.tsundoku.GetCustomListsQuery
import com.tsundoku.UpdateMediaNotesMutation
import com.tsundoku.ViewerQuery
import kotlinx.coroutines.flow.Flow

interface ViewerRepository {
    fun getViewer(): Flow<Result<ViewerQuery.Viewer>>
    fun getCustomLists(userId: Int): Flow<Result<GetCustomListsQuery.MediaList>>
    fun addTsundokuList(customLists: MutableList<String>): Flow<Result<AddTsundokuListMutation.UpdateUser>>
    fun updateMediaNotes(mediaId: Int?, newNote: String): Flow<Result<UpdateMediaNotesMutation.SaveMediaListEntry>>
}