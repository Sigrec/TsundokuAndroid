package com.tsundoku.anilist.collection

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsundoku.GetTsundokuCollectionQuery
import com.tsundoku.data.NetworkResource
import com.tsundoku.data.NetworkResource.Companion.asResource
import com.tsundoku.models.CollectionUiState
import com.tsundoku.models.TsundokuItem
import com.tsundoku.type.MediaListSort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val userRepo: CollectionRepositoryImpl
) : ViewModel() {
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

    val isRefreshing: MutableState<Boolean> = mutableStateOf(false)
    fun setIsRefreshing(new: Boolean) { isRefreshing.value = new }

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

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()
    fun updateSearchText(text: String) {
        Log.d("Collection View Model", "Searching Collection for $text")
        _searchText.value = text
    }

    private val _searchingState: MutableState<Boolean> = mutableStateOf(false)
    val searchingState: State<Boolean> = _searchingState
    fun toggleSearchingState() { _searchingState.value = _searchingState.value xor true }

    private val _tsundokuCollection: MutableStateFlow<MutableList<TsundokuItem>> = MutableStateFlow(mutableListOf())
    @OptIn(FlowPreview::class)
    val tsundokuCollection = searchText
        .debounce(500L)
        .combine(_tsundokuCollection) {text, collection ->
            if (text.isBlank()) {
                collection
            } else {
                collection.filter {
                    it.title.contains(text, ignoreCase = true)
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(1000),
            _tsundokuCollection.value
        )

    fun setTsundokuCollection(newCollection: MutableList<TsundokuItem>) {
        newCollection.sortBy { it.title }
        _tsundokuCollection.update { newCollection }
    }
    fun clearTsundokuCollection() {
        _tsundokuCollection.value.clear()
    }

    fun addItemToTsundokuCollection(item: TsundokuItem) {
        if (_tsundokuCollection.value.size == 0) {
            _tsundokuCollection.value.add(item)
        }
        else {
            var index = _tsundokuCollection.value.binarySearch(item, compareBy { it.title })
            index = if (index < 0) index.inv() else index
            _tsundokuCollection.value.add(index, item)
        }
    }

    fun deleteItemFromTsundokuCollection(item: TsundokuItem) {
        _tsundokuCollection.value.forEachIndexed { index, it ->
            if (it.mediaId == item.mediaId) {
                _tsundokuCollection.value.removeAt(index)
            }
        }
        //_tsundokuCollection.update { _tsundokuCollection.value }
    }
    fun sortTsundokuCollection() { _tsundokuCollection.value.sortBy { it.title } }
}