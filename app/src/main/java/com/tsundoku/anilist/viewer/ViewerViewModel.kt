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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
    private val preferencesRepo: PreferencesRepositoryImpl
) : ViewModel() {

    /**
     * Whether user opening the app has successfully oauth'd
     */
    val isLoggedIn = preferencesRepo.accessToken.map { !it.isNullOrEmpty() }

    /**
     * Authenticated users information, represented as a viewer in AniList GraphQL
     */
    val aniListViewer = viewerRepo.getViewer().asResource().stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), NetworkResource.loading())

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
     *
     */
    fun updateMediaNotes(mediaId: Int?, newNote: String) {
        viewModelScope.launch(Dispatchers.IO) {
            viewerRepo.updateMediaNotes(mediaId = mediaId, newNote = newNote).collect {
                if (it.isSuccess) {
                    Log.i("ANILIST", "Successfully Updated Media $mediaId")
                }
                else {
                    Log.e("ANILIST", "Updating Media $mediaId Failed -> $it")
                }
            }
        }
    }
}