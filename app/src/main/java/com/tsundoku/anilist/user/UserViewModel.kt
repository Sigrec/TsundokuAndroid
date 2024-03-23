package com.tsundoku.anilist.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsundoku.GetTsundokuCollectionQuery
import com.tsundoku.anilist.viewer.ViewerRepositoryImpl
import com.tsundoku.data.NetworkResource
import com.tsundoku.data.NetworkResource.Companion.asResource
import com.tsundoku.type.MediaListSort
import com.tsundoku.ui.model.CollectionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepo: UserRepositoryImpl,
    private val viewerRepo: ViewerRepositoryImpl
) : ViewModel() {
    // Supabase DB password = BWFSuTweiePBRlTL
    private val _collectionUiState = MutableStateFlow(CollectionUiState())
    val collectionUiState: StateFlow<CollectionUiState> = _collectionUiState.asStateFlow()

    fun getTsundokuCollection(viewerId: Int, titleSort: List<MediaListSort?>): StateFlow<NetworkResource<List<GetTsundokuCollectionQuery.List?>>> {
        return if (collectionUiState.value.onViewer) {
            userRepo.getTsundokuCollection(null, viewerId, titleSort).asResource().stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), NetworkResource.loading())
        } else {
            userRepo.getTsundokuCollection(collectionUiState.value.curUser, null, titleSort).asResource().stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), NetworkResource.loading())
        }
    }

    /**
     * Sets the current username for the collection being shown
     * @param username The AniList username to show the collection for
     */
    fun setUsername(username: String) = _collectionUiState.update { it.copy(onViewer = false, curUser = username) }

    /**
     * Sets whether the current collection is showing the Viewer's collection (authenticated user) or a regular user's collection
     * @param value True if currently is the viewer, false otherwise
     */
    fun onViewer(value: Boolean) = _collectionUiState.update { it.copy(onViewer = value) }

    /**
     * Sets the index for the current media that the viewer wants to edit so it opens the composable only if the current collection is the viewer's collection
     * @param index The current index of the media
     */
    fun setCurEditingMediaIndex(index: Int) {
        if (collectionUiState.value.onViewer) {
            _collectionUiState.update { it.copy(curEditingMediaIndex = index) }
        }
    }
}