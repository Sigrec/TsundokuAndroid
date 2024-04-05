package com.tsundoku.anilist.collection

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tsundoku.GetTsundokuCollectionQuery
import com.tsundoku.data.NetworkResource
import com.tsundoku.data.NetworkResource.Companion.asResource
import com.tsundoku.data.TsundokuFilter
import com.tsundoku.data.TsundokuFormat
import com.tsundoku.data.TsundokuStatus
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
    fun updateSearchText(curText: String) {
        _searchText.value = curText
    }
    private val _searchingState: MutableState<Boolean> = mutableStateOf(false)
    val searchingState: State<Boolean> = _searchingState
    fun toggleSearchingState() { _searchingState.value = _searchingState.value xor true }

    private val _filter = MutableStateFlow(TsundokuFilter.NONE)
    val filter = _filter.asStateFlow()
    fun updateFilter(tsundokuFilter: TsundokuFilter) {
        _filter.value = tsundokuFilter
    }
    private val _filteringState: MutableState<Boolean> = mutableStateOf(false)
    val filteringState: State<Boolean> = _filteringState
    fun toggleFilteringState() { _filteringState.value = _filteringState.value xor true }

    private val _tsundokuCollection: MutableStateFlow<MutableList<TsundokuItem>> = MutableStateFlow(mutableStateListOf())
    @OptIn(FlowPreview::class)
    val tsundokuCollection: StateFlow<List<TsundokuItem>> = combine(
        searchText.debounce { if (it.isNotBlank()) 800L else 500L },
        filter.debounce(100L),
        _tsundokuCollection.asStateFlow()
    )
    { text, curFilter, collection ->
            if (text.isNotBlank()) {
                collection.filter {
                    it.title.contains(text, ignoreCase = true)
                }
            }
            else if (curFilter != TsundokuFilter.NONE) {
                collection.filter {
                    when (curFilter) {
                        TsundokuFilter.MANGA -> it.format == TsundokuFormat.MANGA || it.format == TsundokuFormat.MANHWA || it.format == TsundokuFormat.MANHUA || it.format == TsundokuFormat.COMIC || it.format == TsundokuFormat.MANFRA
                        TsundokuFilter.NOVEL -> it.format == TsundokuFormat.NOVEL
                        TsundokuFilter.ONGOING -> it.status == TsundokuStatus.ONGOING
                        TsundokuFilter.FINISHED -> it.status == TsundokuStatus.FINISHED
                        TsundokuFilter.COMING_SOON -> it.status == TsundokuStatus.COMING_SOON
                        TsundokuFilter.CANCELLED -> it.status == TsundokuStatus.CANCELLED
                        TsundokuFilter.HIATUS -> it.status == TsundokuStatus.HIATUS
                        TsundokuFilter.COMPLETE -> it.curVolumes.value.toInt() == it.maxVolumes.value.toInt()
                        TsundokuFilter.INCOMPLETE -> it.curVolumes.value.toInt() != it.maxVolumes.value.toInt()
                        else -> true
                    }
                }
            }
            else {
                collection
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
            val index = _tsundokuCollection.value.binarySearch(item, compareBy { it.title })
            _tsundokuCollection.value.add(if(index < 0) index.inv() else index, item)
        }
    }
    fun deleteItemFromTsundokuCollection(index: Int) {
        _tsundokuCollection.value.removeAt(index)
    }
    fun deleteItemFromTsundokuCollection(item: TsundokuItem) {
        _tsundokuCollection.update { (it - item).toMutableList() }
    }
    fun sortTsundokuCollection() { _tsundokuCollection.value.sortBy { it.title } }
    fun getTsundokuItemIndex(item: TsundokuItem): Int {
        return _tsundokuCollection.value.indexOf(item)
    }
}