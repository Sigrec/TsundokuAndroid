package com.tsundoku.anilist.viewer

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsundoku.GetCustomListsQuery
import com.tsundoku.TSUNDOKU_SCHEME
import com.tsundoku.anilist.preferences.PreferencesRepositoryImpl
import com.tsundoku.data.NetworkResource
import com.tsundoku.data.NetworkResource.Companion.asResource
import com.tsundoku.models.Media
import com.tsundoku.models.TsundokuItem
import com.tsundoku.models.ViewerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Holds functions and variables related to updating & accessing user (viewer) info
 */
@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val viewerRepo: ViewerRepositoryImpl,
    private val preferencesRepo: PreferencesRepositoryImpl,
) : ViewModel() {

    private val _viewerState = MutableStateFlow(ViewerState())
    val viewerState : StateFlow<ViewerState> = _viewerState.asStateFlow()

    fun setViewerId(id: Int) { _viewerState.update { it.copy(viewerId = id) } }
    fun getViewerId(): Int = viewerState.value.viewerId

    fun setCurrencyCode(code: String) { _viewerState.update { it.copy(currencyCode = code) } }
    fun setCurrencySymbol(symbol: String) { _viewerState.update { it.copy(currencySymbol = symbol) } }


    fun setTsundokuCollection(newCollection: MutableList<TsundokuItem>) = _viewerState.update { it.copy(collection = newCollection) }
    fun sortTsundokuCollection() { viewerState.value.collection?.sortBy { it.title } }

    /**
     * Whether user opening the app has successfully oauth'd
     */
    val isLoggedIn = preferencesRepo.accessToken.map { !it.isNullOrEmpty() }

    /**
     * Authenticated users information, represented as a viewer in AniList GraphQL
     */
    val aniListViewer = viewerRepo.getAniListViewer().asResource().stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), NetworkResource.loading())


    /**
     * Gets the logged in users custom list(s)
     */
    fun getCustomLists(viewerId: Int): StateFlow<NetworkResource<GetCustomListsQuery.MediaList>> {
        return viewerRepo.getCustomLists(viewerId).asResource().stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), NetworkResource.loading())
    }

    /**
     * Adds the "Tsundoku" custom list to the logged in users AniList profile
     */
    fun addTsundokuList(customLists: List<String>) {
        viewModelScope.launch {
            viewerRepo.addTsundokuList(customLists.toMutableList()).collect {
                if (it.isSuccess) {
                    Log.i("ANILIST", "Successfully Added Tsundoku Custom List")
                }
                else {
                    Log.e("ANILIST", "Adding Tsundoku Custom List Failed")
                }
            }
        }
    }

    /**
     * When the redirect intent returns with the users access token save it datastore
     * @param data The redirect url containing the access token
     */
    fun onTokenDataReceived(data: Uri?) = viewModelScope.launch(Dispatchers.IO) {
        if (data?.scheme == TSUNDOKU_SCHEME && data.fragment?.startsWith("access_token") == true) {
            Log.d("ANILIST", "Fetching User Token")
            preferencesRepo.setAccessToken(data.fragment!!.substringAfter("access_token=").substringBefore("&token_type"))
        }
    }

    /**
     * Updates the viewers notes for a specific series
     * @param mediaId The unique id of the media to update
     * @param newNote The new note to update to for the media
     */
    fun updateAniListMediaNotes(mediaId: String, newNote: String) {
        viewModelScope.launch(Dispatchers.IO) {
            viewerRepo.updateAniListMediaNotes(mediaId = mediaId.toInt(), newNote = newNote)
            .collect {
                if (it.isSuccess) {
                    Log.i("ANILIST", "Successfully Updated Media $mediaId")
                } else {
                    Log.e("ANILIST", "Updating Media $mediaId Failed -> $it")
                }
            }
        }
    }

    suspend fun updateDatabaseMedia(viewerId: Int, mediaId: String, updateMap: Map<String, Any?>) {
        viewerRepo.updateMedia(viewerId, mediaId, updateMap)
    }

    suspend fun getCurrencyCode(viewerId: Int): String? {
       return viewerRepo.getDatabaseViewerCurrencyCode(viewerId)?.currency
    }

    suspend fun insertDatabaseViewer(viewerId: Int) {
        viewerRepo.insertDatabaseViewer(viewerId)
    }

    // REMEMBER TO GET THE CURRENT ANILIST STATUS?
    suspend fun insertNewMedia(mediaList: List<Media>) {
        viewerRepo.insertNewMedia(mediaList)
    }

    suspend fun getDatabaseMediaList(viewerId: Int): List<Media> {
        return viewerRepo.getMediaList(viewerId)
    }
}