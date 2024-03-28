package com.tsundoku.ui.collection

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.tsundoku.GetTsundokuCollectionQuery
import com.tsundoku.anilist.collection.CollectionViewModel
import com.tsundoku.anilist.viewer.ViewerViewModel
import com.tsundoku.data.NetworkResource
import com.tsundoku.models.Media
import com.tsundoku.models.MediaModel
import com.tsundoku.models.TsundokuItem
import com.tsundoku.models.ViewerModel
import com.tsundoku.models.Website
import com.tsundoku.ui.loading.LoadingIndicator
import com.tsundoku.ui.loading.LoadingScreen
import java.math.BigDecimal

@Destination(route = "collection")
@Composable
fun CollectionScreen(
    viewerViewModel: ViewerViewModel,
    collectionViewModel: CollectionViewModel,
) {
    // TODO - Currently going back and forth between profile and collection causes it to load the collection again, not a big issue right now
    LaunchedEffect(Unit) {
        instantiateDatabaseUser(viewerViewModel)
        fetchTsundokuCollection(viewerViewModel, collectionViewModel)
    }
    val collectionUiState by collectionViewModel.collectionUiState.collectAsState()
    val tsundokuCollection by collectionViewModel.tsundokuCollection.collectAsState()

    Surface (
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF13171D),
    ) {
        LazyColumn(
            modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            if (collectionUiState.onViewer) {
                Log.d("Collection Screen", "Showing Viewer Collection")
                itemsIndexed(items = tsundokuCollection, key = { _, item -> item.mediaId }) { index, item ->
                    SwipeMediaCardContainer(item = item, mediaCard = { MediaCard(item = item, index = index, viewerViewModel, collectionUiState, LocalUriHandler.current) }, viewerViewModel = viewerViewModel, collectionViewModel = collectionViewModel)
                }
            }
            else {
                Log.d("Collection Screen", "Showing Another Users Collection")
                itemsIndexed(items = tsundokuCollection, key = { _, item -> item.mediaId }) { index, item ->
                    MediaCard(item = item, index = index, viewerViewModel, collectionUiState, LocalUriHandler.current)
                }
            }
        }
    }

    if (viewerViewModel.selectedItemIndex.intValue > -1) {
        MediaEditScreen(item = tsundokuCollection[viewerViewModel.selectedItemIndex.intValue], coroutineScope = rememberCoroutineScope(), viewerViewModel = viewerViewModel)
    }

    if (viewerViewModel.isLoading.value){
        LoadingScreen()
    }

    if (collectionViewModel.isRefreshing.value) LoadingIndicator()
}

/**
 * Checks the database to see whether the Tsundoku Collection has matching entries in the database for the user, if a media entry is not in the database for the user then we add it
 * @param viewerId Unique id of the authenticated used
 * @param tsundokuCollection The users current Tsundoku Collection from AniList
 */
suspend fun updateDatabaseMediaList(viewerId: Int, tsundokuCollection: List<GetTsundokuCollectionQuery.Entry?>, viewerViewModel: ViewerViewModel) {
    val curDbMediaList = viewerViewModel.getDatabaseMediaList(viewerId)
    val insertList: MutableList<Media> = mutableListOf()
    val deleteList: MutableList<String> = mutableListOf()
    if (curDbMediaList.isEmpty()) {
        tsundokuCollection.forEach {
            insertList.add(
                Media(
                    viewerId,
                    it!!.mediaListEntry.mediaId.toString(),
                    0,
                    it.mediaListEntry.media!!.volumes ?: 1,
                    BigDecimal(0.00)
                )
            )
        }
    } else {
        // Check to see if a specific media in the Tsundoku Collection exists in the DB if not add it to be batched
        tsundokuCollection.forEach {
            val mediaId = it!!.mediaListEntry.mediaId.toString()
            if (!curDbMediaList.parallelStream().anyMatch { media -> media.mediaId == mediaId }) {
                insertList.add(
                    Media(
                        viewerId,
                        mediaId,
                        0,
                        it.mediaListEntry.media!!.volumes ?: 1,
                        BigDecimal(0.00)
                    )
                )
            }
        }

        // Check to see if a media entry exists in the DB for a series that is no longer in the users collection if so add it to delete batch list
        curDbMediaList.forEach {
            if (!tsundokuCollection.parallelStream().anyMatch { media -> media!!.mediaListEntry.mediaId.toString() == it.mediaId }) {
                deleteList.add(it.mediaId)
            }
        }
    }

    if (insertList.isNotEmpty()) {
        Log.i("Supabase", "Updating Database List for Viewer $viewerId")
        viewerViewModel.insertNewDatabaseMedia(insertList)
    }

    if (deleteList.isNotEmpty()) {
        Log.i("Supabase", "Batch Removing Media Entries for Viewer $viewerId")
        viewerViewModel.deleteDatabaseMedia(deleteList)
    }
}

/**
 * Checks whether the current authenticated user has been onboarded and if not inserts them and then gets the currency code (default is "USD") of the authenticated user and sets the currency symbol (default is "$")
 * @param viewerViewModel The view model for the authenticated user
 */
suspend fun instantiateDatabaseUser(viewerViewModel: ViewerViewModel) {
    val viewerId = viewerViewModel.getViewerId()
    val currencyCode = viewerViewModel.getDatabaseCurrencyCode(viewerId)
    if (currencyCode != null) {
        Log.d("Supabase", "Getting User $viewerId Currency Code")
        viewerViewModel.setCurrencyCode(currencyCode)
        viewerViewModel.setCurrencySymbol(MediaModel.getCurrencySymbol(currencyCode))
    } else {
        Log.d("Supabase", "Added new Viewer $viewerId to Database")
        viewerViewModel.insertDatabaseViewer(viewerId)
    }
}

/**
 * Gets the users tsundoku collection from AniList & Supabase
 */
suspend fun fetchTsundokuCollection(viewerViewModel: ViewerViewModel, collectionViewModel: CollectionViewModel) {
    if (collectionViewModel.isRefreshing.value) ViewerModel.batchUpdateMediaVolumeCount(viewerViewModel, collectionViewModel)
    val viewerId = viewerViewModel.getViewerId()
    collectionViewModel.getTsundokuCollection(viewerId, getMediaListSort(viewerViewModel.getPreferredLang().name)).collect { response ->
        when (response) {
            is NetworkResource.Success -> {
                Log.d("Tsundoku", "Instantiating User Collection")
                val collection: MutableList<TsundokuItem> = mutableListOf()
                val aniListEntries = response.data[0]!!.entries!!
                updateDatabaseMediaList(viewerId, aniListEntries, viewerViewModel)
                val viewerDatabaseMediaList = viewerViewModel.getDatabaseMediaList(viewerId)

                // Add AniList Entries
                aniListEntries.forEach { entry ->
                    val media = entry!!.mediaListEntry.media
                    val dbMedia = viewerDatabaseMediaList.parallelStream().filter {
                        it.mediaId == entry.mediaListEntry.mediaId.toString()
                    }.findAny().get()
                    // Log.d("TEST", "${media!!.title!!.userPreferred!!} | ${dbMedia.curVolumes} | ${dbMedia.maxVolumes} | ${dbMedia.cost} | ${dbMedia.notes}")
                    collection.add(
                        TsundokuItem(
                            mediaId = entry.mediaListEntry.mediaId.toString(),
                            Website.ANILIST,
                            media!!.title!!.userPreferred!!,
                            media.countryOfOrigin.toString(),
                            MediaModel.getCorrectFormat(
                                media.format!!.name,
                                media.countryOfOrigin.toString()
                            ),
                            media.chapters ?: 0,
                            entry.mediaListEntry.notes ?: "",
                            media.coverImage!!.medium!!,
                            curVolumes = mutableStateOf(dbMedia.curVolumes.toString()),
                            maxVolumes = mutableStateOf(dbMedia.maxVolumes.toString()),
                            cost = dbMedia.cost
                        )
                    )
                }

                // Add MangaDex Entries
                collectionViewModel.setTsundokuCollection(collection)
                // collectionViewModel.sortTsundokuCollection() enable if adding MangaDex
                if (collectionViewModel.isRefreshing.value) collectionViewModel.setIsRefreshing(false)
                if (viewerViewModel.isLoading.value) {
                    Log.d("TEST", "TOGGLING OFF LOADING SCREEN")
//                    viewerViewModel.toggleTopAppBar()
//                    viewerViewModel.toggleBottomAppBar()
                    viewerViewModel.setIsLoading(false)
                }
            }
            is NetworkResource.Loading -> {
                Log.i("Collecting Screen", "Loading Tsundoku Collection")
            }
            else -> {
                Log.e("Collecting Screen", "Unknown Error Getting Tsundoku Collection")
            }
        }
    }
}

