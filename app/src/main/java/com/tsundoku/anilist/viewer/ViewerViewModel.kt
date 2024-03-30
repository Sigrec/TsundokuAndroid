package com.tsundoku.anilist.viewer

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsundoku.APP_NAME
import com.tsundoku.GetCustomListsQuery
import com.tsundoku.GetMediaCustomListsQuery
import com.tsundoku.TSUNDOKU_SCHEME
import com.tsundoku.anilist.enums.Lang
import com.tsundoku.anilist.preferences.PreferencesRepositoryImpl
import com.tsundoku.data.NetworkResource
import com.tsundoku.data.NetworkResource.Companion.asResource
import com.tsundoku.data.TsundokuFormat
import com.tsundoku.models.Media
import com.tsundoku.models.ViewerModel
import com.tsundoku.models.ViewerState
import com.tsundoku.models.VolumeUpdateMedia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    private val _viewerState = mutableStateOf(ViewerState())
    val viewerState: ViewerState = _viewerState.value

    // Items that had there volume count updated in the collection screen
    private val _updatedCollectionItems: MutableStateFlow<MutableList<String>> = MutableStateFlow(mutableListOf())
    val updatedCollectionItems = _updatedCollectionItems
    fun addUpdatedCollectionItem(mediaId: String) {
        if (!_updatedCollectionItems.value.parallelStream().anyMatch { it == mediaId }) {
            _updatedCollectionItems.value.add(mediaId)
        }
    }

    val isLoading: MutableState<Boolean> = mutableStateOf(false)
    fun setIsLoading(new: Boolean) { isLoading.value = new }

    val selectedItemIndex: MutableIntState = mutableIntStateOf(-1)
    fun setSelectedItemIndex(index: Int) { selectedItemIndex.intValue = index }

    fun setViewerId(id: Int) { _viewerState.value.viewerId = id }
    fun getViewerId(): Int = _viewerState.value.viewerId

    fun setPreferredLang(lang: Lang) { _viewerState.value.preferredLang= lang }
    fun getPreferredLang(): Lang = _viewerState.value.preferredLang

    fun setCurrencyCode(code: String) { _viewerState.value.currencyCode = code }
    fun getCurrencyCode(): String = _viewerState.value.currencyCode

    fun setCurrencySymbol(symbol: String) { _viewerState.value.currencySymbol = symbol }
    fun getCurrencySymbol(): String = _viewerState.value.currencySymbol

//    fun setSelectedPaneIndex(index: Int) { _viewerState.value.selectedPaneIndex = index }
//    fun getSelectedPaneIndex(): Int = _viewerState.value.selectedPaneIndex

    val showTopAppBar: MutableState<Boolean> = mutableStateOf(true)
    fun turnOffTopAppBar() { showTopAppBar.value = false }
    fun turnOnTopAppBar() { showTopAppBar.value = true }

    val showBottomAppBar: MutableState<Boolean> = mutableStateOf(true)
    private fun turnOffBottomAppBar() { showBottomAppBar.value = false }
    private fun turnOnBottomAppBar() { showBottomAppBar.value = true }

    fun turnOffAppBar(){
        if (showTopAppBar.value) turnOffTopAppBar()
        if (showBottomAppBar.value) turnOffBottomAppBar()
    }

    fun turnOnAppBar(){
        if (!showTopAppBar.value) turnOnTopAppBar()
        if (!showBottomAppBar.value) turnOnBottomAppBar()
    }

    /**
     * Whether user opening the app has successfully oauth'd
     */
    val isLoggedIn = preferencesRepo.accessToken.map { !it.isNullOrEmpty() }

    /**
     * Authenticated users information, represented as a viewer in AniList GraphQL
     */
    val aniListViewer = viewerRepo.getAniListViewer().asResource().stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), NetworkResource.loading())

    fun getNewAniListMediaSeries(seriesId: Int?, title: String?, format: TsundokuFormat) = viewerRepo.getNewAniListMediaSeries(seriesId, title, format).asResource().stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), NetworkResource.loading())

    /**
     * Gets the logged in users custom list(s)
     */
    fun getViewerCustomLists(viewerId: Int): StateFlow<NetworkResource<GetCustomListsQuery.MediaList>> {
        return viewerRepo.getViewerCustomLists(viewerId).asResource().stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), NetworkResource.loading())
    }

    /**
     * Gets the authenticated users custom lists for a specific media
     */
    fun getMediaCustomLists(mediaId: Int): StateFlow<NetworkResource<GetMediaCustomListsQuery.MediaList>> {
        return viewerRepo.getMediaCustomLists(viewerState.viewerId, mediaId).asResource().stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), NetworkResource.loading())
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
            Log.d("AniList", "Fetching User Token")
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

    /**
     * Adds a AniList media to "Tsundoku" custom list in AniList
     * @param mediaId The unique AniList media ID for the series
     */
    fun addAniListMediaToCollection(mediaId: Int, customLists: MutableList<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            customLists.add(APP_NAME)
            Log.d("TEST", "CUSTOM LISTS 1 = ${ViewerModel.parseTrueCustomLists(StringBuilder(customLists.toString().trim()))}")
            viewerRepo.addAniListMediaToCollection(mediaId = mediaId, customLists = customLists)
            .collect {
                if(it.isSuccess) {
                    Log.i(APP_NAME, "Successfully Added Media $mediaId To Collection")
                } else if (it.isFailure) {
                    Log.e(APP_NAME, "Adding Media $mediaId To Collection Failed")
                } else {
                    Log.e(APP_NAME, "Adding Media $mediaId Unknown Issues/Error")
                }
            }
        }
    }

    /**
     * Deletes a AniList media series from the authenticated users "Tsundoku" custom list in AniList
     * @param mediaId The unique AniList media ID for the series
     * @param customLists The users current list of custom lists to not override others
     */
    fun deleteAniListMediaFromCollection(mediaId: Int, customLists: MutableList<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            customLists.remove(APP_NAME)
            viewerRepo.deleteAniListMediaFromCollection(mediaId = mediaId, customLists = customLists)
                .collect {
                    if (it.isSuccess) {
                        Log.i("ANILIST", "Successfully Deleted Media $mediaId From Collection")
                    } else {
                        Log.e("ANILIST", "Deleting Media $mediaId From Collection Failed -> $it")
                    }
                }
        }
    }

    /**
     * Updates a media entry in the database for the authenticated user
     * @param viewerId The authenticated users AniList ID
     * @param mediaId The unique ID for the entry
     * @param updateMap Map containing the updates to make to the media
     */
    suspend fun updateDatabaseMedia(viewerId: Int, mediaId: String, updateMap: Map<String, Any?>) = viewerRepo.updateMedia(viewerId, mediaId, updateMap)

    /**
     * Updates a media entry in the database for the authenticated user
     * @param updateList List of media and the new curVolume count to update to
     */
    suspend fun batchUpdateDatabaseMedia(updateList: MutableList<VolumeUpdateMedia>) = viewerRepo.batchCurVolumesUpdateMedia(_viewerState.value.viewerId, updateList)

    /**
     * Gets the currency currency code of the authenticated user
     * @param viewerId The authenticated users AniList ID
     */
    suspend fun getDatabaseCurrencyCode(viewerId: Int): String? {
       return viewerRepo.getDatabaseViewerCurrencyCode(viewerId)?.currency
    }

    /**
     * Inserts a new user into the database
     * @param viewerId The authenticated users AniList ID
     */
    suspend fun insertDatabaseViewer(viewerId: Int) {
        viewerRepo.insertDatabaseViewer(viewerId)
    }

    /**
     * Insert a list of new media entries for a user into the database
     * @param mediaList The list of media to add for the user
     */
    suspend fun insertNewDatabaseMedia(mediaList: List<Media>) {
        viewerRepo.insertNewMedia(mediaList)
    }

    /**
     * Gets the list of media for a users collection
     * @param viewerId The authenticated users AniList ID
     */
    suspend fun getDatabaseMediaList(viewerId: Int): List<Media> {
        return viewerRepo.getMediaList(viewerId)
    }

    suspend fun deleteDatabaseMedia(deleteList: List<String>) {
        viewerRepo.deleteMedia(_viewerState.value.viewerId, deleteList)
    }
}