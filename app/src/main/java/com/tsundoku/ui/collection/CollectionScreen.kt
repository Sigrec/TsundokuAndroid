package com.tsundoku.ui.collection

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.tsundoku.APP_NAME
import com.tsundoku.GetTsundokuCollectionQuery
import com.tsundoku.TSUNDOKU_COLLECTION_CARD_GAP
import com.tsundoku.anilist.collection.CollectionViewModel
import com.tsundoku.anilist.viewer.ViewerViewModel
import com.tsundoku.data.NetworkResource
import com.tsundoku.models.Media
import com.tsundoku.models.MediaModel
import com.tsundoku.models.TsundokuItem
import com.tsundoku.models.ViewerModel
import com.tsundoku.ui.loading.LoadingIndicator
import com.tsundoku.ui.loading.LoadingScreen
import java.math.BigDecimal
import java.math.RoundingMode

@Destination(route = "collection")
@Composable
fun CollectionScreen(
    viewerViewModel: ViewerViewModel,
    collectionViewModel: CollectionViewModel
) {
    if (viewerViewModel.isLoading.value) {
        LaunchedEffect(Unit) {
            Log.d("TEST", "Loading Collection")
            instantiateDatabaseUser(viewerViewModel)
            fetchTsundokuCollection(viewerViewModel, collectionViewModel)
        }
    }

    if (!viewerViewModel.showTopAppBar.value) viewerViewModel.turnOnTopAppBar()

    val collectionUiState by collectionViewModel.collectionUiState.collectAsState()
    val tsundokuCollection by collectionViewModel.tsundokuCollection.collectAsState()
    val searchingState by collectionViewModel.searchingState
    val filteringState by collectionViewModel.filteringState

    if (viewerViewModel.selectedItemIndex.intValue > -1) {
        if (viewerViewModel.showTopAppBar.value) viewerViewModel.turnOffTopAppBar()
        MediaEditScreen(item = tsundokuCollection[viewerViewModel.selectedItemIndex.intValue], coroutineScope = rememberCoroutineScope(), viewerViewModel = viewerViewModel)
    }
    else {
        Surface (
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF13171D),
        ) {
            // Set state functionality for filtering
            // TODO - Srcollstate not saved when filtering then coming back
            // TODO - Multiple reload when opening edit media pane
            LazyColumn(
                modifier = Modifier
                    .padding(0.dp, if(searchingState || filteringState) TSUNDOKU_COLLECTION_CARD_GAP else 0.dp, 0.dp, 10.dp),
                verticalArrangement = Arrangement.spacedBy(TSUNDOKU_COLLECTION_CARD_GAP),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (collectionUiState.onViewer) {
                    Log.d(APP_NAME, "Showing Viewer Collection")
                    itemsIndexed(tsundokuCollection, key = { _, item -> item.mediaId }) { _, item ->
                        SwipeMediaCardContainer(item = item, mediaCard = { MediaCard(item = item, viewerViewModel, collectionViewModel, collectionUiState, LocalUriHandler.current) }, viewerViewModel = viewerViewModel, collectionViewModel = collectionViewModel)
                    }
                }
                else {
                    Log.d(APP_NAME, "Showing Another Users Collection")
                    items(tsundokuCollection, key = { item -> item.mediaId }) { item ->
                        MediaCard(item = item, viewerViewModel, collectionViewModel, collectionUiState, LocalUriHandler.current)
                    }
                }
            }
        }
    }

    if (viewerViewModel.isLoading.value){
        LoadingScreen()
    }
    else if (collectionViewModel.isRefreshing.value) LoadingIndicator()
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
                    it!!.mediaListEntry.media!!.id.toString(),
                    0,
                    it.mediaListEntry.media!!.volumes ?: 1,
                    BigDecimal(0.00)
                )
            )
        }
    } else {
        // Check to see if a specific media in the Tsundoku Collection exists in the DB if not add it to be batched
        tsundokuCollection.forEach {
            val curMedia = it!!.mediaListEntry.media!!
            val mediaId = curMedia.id.toString()
            if (!curDbMediaList.parallelStream().anyMatch { media -> media.mediaId == mediaId }) {
                insertList.add(
                    Media(
                        viewerId,
                        mediaId,
                        0,
                        curMedia.volumes ?: 1,
                        BigDecimal(0.00)
                    )
                )
            }
        }

        // Check to see if a media entry exists in the DB for a series that is no longer in the users collection if so add it to delete batch list
        curDbMediaList.forEach {
            if (!tsundokuCollection.parallelStream().anyMatch { media -> media!!.mediaListEntry.media!!.id.toString() == it.mediaId }) {
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
        val symbol = MediaModel.getCurrencySymbol(currencyCode)
        Log.i("Supabase", "Getting User $viewerId Currency Code $currencyCode & Symbol $symbol")
        viewerViewModel.setCurrencyCode(currencyCode)
        viewerViewModel.setCurrencySymbol(symbol)
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
                var cost = BigDecimal(0.00).setScale(2, RoundingMode.HALF_UP)
                var volumes = 0
                var chapters = 0

                // Add AniList Entries
                for (entry in aniListEntries) {
                    Log.d("TEST", "${entry!!.mediaListEntry.media!!.mediaListEntry?.score}")
                    val dbMedia = viewerDatabaseMediaList.parallelStream().filter {
                        it.mediaId == entry!!.mediaListEntry.media!!.id.toString()
                    }.findAny().get()

                    cost = cost.plus(dbMedia.cost)
                    volumes += dbMedia.curVolumes
                    chapters += entry!!.mediaListEntry.media!!.chapters ?: 0
                    collection.add(MediaModel.parseAniListMedia(entry.mediaListEntry.media!!, dbMedia))
                }

                // Add MangaDex Entries
                collectionViewModel.setTsundokuCollection(collection)
                // collectionViewModel.sortTsundokuCollection() enable if adding MangaDex
                if (collectionViewModel.isRefreshing.value) collectionViewModel.setIsRefreshing(false)
                if (viewerViewModel.isLoading.value) {
                    viewerViewModel.turnOnAppBar()
                    viewerViewModel.setIsLoading(false)
                }
                viewerViewModel.setSeriesCount(aniListEntries.size)
                viewerViewModel.setCollectionCost(cost)
                viewerViewModel.setVolumeCount(volumes)
                viewerViewModel.setChapterCount(chapters)
            }
            is NetworkResource.Loading -> {
                Log.i(APP_NAME, "Loading Tsundoku Collection")
            }
            else -> {
                Log.e(APP_NAME, "Unknown Error Getting Tsundoku Collection")
            }
        }
    }
}

