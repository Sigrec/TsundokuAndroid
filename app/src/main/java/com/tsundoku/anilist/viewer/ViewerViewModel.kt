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
import com.tsundoku.ViewerQuery
import com.tsundoku.anilist.enums.Lang
import com.tsundoku.anilist.preferences.PreferencesRepositoryImpl
import com.tsundoku.data.NetworkResource
import com.tsundoku.data.NetworkResource.Companion.asResource
import com.tsundoku.data.TsundokuFormat
import com.tsundoku.data.TsundokuStatus
import com.tsundoku.models.Media
import com.tsundoku.models.TsundokuItem
import com.tsundoku.models.ViewerModel
import com.tsundoku.models.ViewerState
import com.tsundoku.models.VolumeUpdateMedia
import com.tsundoku.type.MediaListStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

/**
 * Holds functions and variables related to updating & accessing user (viewer) info
 */
@HiltViewModel
class ViewerViewModel @Inject constructor(
    private val viewerRepo: ViewerRepositoryImpl,
    private val preferencesRepo: PreferencesRepositoryImpl,
) : ViewModel() {

    /**
     * Whether user opening the app has successfully oauth'd
     */
    val isLoggedIn = preferencesRepo.accessToken.map { !it.isNullOrEmpty() }

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

    private val _viewerState = MutableStateFlow(ViewerState())
    val viewerState: StateFlow<ViewerState> = _viewerState.asStateFlow()

    fun logOut(refresh: () -> Unit) {
        Log.i(APP_NAME, "Logging Viewer ${_viewerState.value.viewerId} Out")
        viewModelScope.launch {
            preferencesRepo.logOutViewer()
            refresh()
        }
    }

    // Series Count
    fun incrementSeriesCount() = _viewerState.value.seriesCount++
    fun decrementSeriesCount() = _viewerState.value.seriesCount--
    fun setSeriesCount(count: Int) { _viewerState.value.seriesCount = count }

    // Collection Cost/Value
    fun setCollectionCost(value: BigDecimal) { _viewerState.value.collectionCost = value }
    fun decreaseCollectionCost(value: BigDecimal) { _viewerState.value.collectionCost = _viewerState.value.collectionCost.minus(value) }
    fun increaseCollectionCost(value: BigDecimal) { _viewerState.value.collectionCost = _viewerState.value.collectionCost.plus(value) }

    // Chapters
    fun setChapterCount(count: Int) { _viewerState.value.chapters = count }
    fun decreaseChapterCount(count: Int) { _viewerState.value.chapters -= count}
    fun increaseChapterCount(count: Int) { _viewerState.value.chapters += count}

    // Volumes
    fun setVolumeCount(count: Int) { _viewerState.value.volumes = count }
    fun decreaseVolumesCount(count: Int) { _viewerState.value.volumes -= count}
    fun increaseVolumeCount(count: Int) { _viewerState.value.volumes += count}

    // Status [finished, ongoing, cancelled, hiatus, coming soon]
    /**
     * Gets the status data for the viewer as a list in the order of [finished, ongoing, cancelled, hiatus, coming soon]
     */
    fun getStatusData(): List<Float> {
        return listOf(_viewerState.value.finishedCount, _viewerState.value.ongoingCount, _viewerState.value.cancelledCount, _viewerState.value.hiatusCount, _viewerState.value.comingSoonCount)
    }
    fun setStatusData(finishedCount: Float? = null, ongoingCount: Float? = null, cancelledCount: Float? = null, hiatusCount: Float? = null, comingSoonCount: Float? = null) {
        if (finishedCount != null && finishedCount >= 0) _viewerState.value.finishedCount = finishedCount
        if (ongoingCount != null && ongoingCount >= 0) _viewerState.value.ongoingCount = ongoingCount
        if (cancelledCount != null && cancelledCount >= 0) _viewerState.value.cancelledCount = cancelledCount
        if (hiatusCount != null && hiatusCount >= 0) _viewerState.value.hiatusCount = hiatusCount
        if (comingSoonCount != null && comingSoonCount >= 0) _viewerState.value.comingSoonCount = comingSoonCount
    }
    fun incrementStatusCount(item: TsundokuItem) {
        when(item.status) {
            TsundokuStatus.FINISHED -> _viewerState.value.finishedCount++
            TsundokuStatus.ONGOING -> _viewerState.value.ongoingCount++
            TsundokuStatus.CANCELLED -> _viewerState.value.cancelledCount++
            TsundokuStatus.HIATUS -> _viewerState.value.hiatusCount++
            TsundokuStatus.COMING_SOON -> _viewerState.value.comingSoonCount++
            TsundokuStatus.ERROR -> Log.e(APP_NAME, "Error with Item Status ${item.status} for [${item.title} ${item.mediaId}]")
        }
    }
    fun decrementStatusCount(item: TsundokuItem) {
        when(item.status) {
            TsundokuStatus.FINISHED -> _viewerState.value.finishedCount--
            TsundokuStatus.ONGOING -> _viewerState.value.ongoingCount--
            TsundokuStatus.CANCELLED -> _viewerState.value.cancelledCount--
            TsundokuStatus.HIATUS -> _viewerState.value.hiatusCount--
            TsundokuStatus.COMING_SOON -> _viewerState.value.comingSoonCount--
            TsundokuStatus.ERROR -> Log.e(APP_NAME, "Error with Item Status ${item.status} for [${item.title} ${item.mediaId}]")
        }
    }

    /**
     * Items that had there volume count updated in the collection screen
     */
    // TODO - Create timed function that executes update after a given time
    private val _updatedCollectionItems: MutableStateFlow<MutableList<String>> = MutableStateFlow(mutableListOf())
    val updatedCollectionItems = _updatedCollectionItems
    fun addUpdatedCollectionItem(mediaId: String) {
        if (!_updatedCollectionItems.value.parallelStream().anyMatch { it == mediaId }) {
            _updatedCollectionItems.value.add(mediaId)
        }
    }

    fun setViewerData(viewer: ViewerQuery.Viewer) {
        _viewerState.value.viewer = viewer
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

    val showTopAppBar: MutableState<Boolean> = mutableStateOf(true)
    fun turnOffTopAppBar() { if (showTopAppBar.value) showTopAppBar.value = false }
    fun turnOnTopAppBar() { if (showBottomAppBar.value) showTopAppBar.value = true }

    val showBottomAppBar: MutableState<Boolean> = mutableStateOf(true)
    private fun turnOffBottomAppBar() { if (!showTopAppBar.value) showBottomAppBar.value = false }
    private fun turnOnBottomAppBar() { if (!showBottomAppBar.value) showBottomAppBar.value = true }

    fun turnOffAppBar(){
        turnOffTopAppBar()
        turnOffBottomAppBar()
    }

    fun turnOnAppBar(){
        turnOnTopAppBar()
        turnOnBottomAppBar()
    }

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
        return viewerRepo.getMediaCustomLists(viewerState.value.viewerId, mediaId).asResource().stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), NetworkResource.loading())
    }

    /**
     * Adds the APP_NAME custom list to the logged in users AniList profile
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
     * Adds a AniList media to APP_NAME custom list in AniList
     * @param mediaId The unique AniList media ID for the series
     */
    fun addAniListMediaToCollection(mediaId: Int, customLists: MutableList<String>, status: MediaListStatus?) {
        viewModelScope.launch(Dispatchers.IO) {
            customLists.add(APP_NAME)
            Log.d("TEST", "CUSTOM LISTS 1 = ${ViewerModel.parseTrueCustomLists(StringBuilder(customLists.toString().trim()))}")
            viewerRepo.addAniListMediaToCollection(mediaId = mediaId, customLists = customLists, status = status)
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
     * Deletes a AniList media series from the authenticated users APP_NAME custom list in AniList
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

    suspend fun updateCurrencyCode(currencyCode: String) {
        viewerRepo.updateCurrencyCode(_viewerState.value.viewerId, currencyCode)
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