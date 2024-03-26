package com.tsundoku.ui.collection

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.tsundoku.R
import com.tsundoku.anilist.user.UserViewModel
import com.tsundoku.anilist.viewer.ViewerViewModel
import com.tsundoku.data.NetworkResource
import com.tsundoku.models.CollectionUiState
import com.tsundoku.models.Media
import com.tsundoku.models.MediaModel
import com.tsundoku.models.TsundokuItem
import com.tsundoku.models.Website
import com.tsundoku.ui.BottomNavigationItem
import com.tsundoku.ui.theme.TsundokuBackground

@OptIn(ExperimentalMaterial3Api::class)
@Destination(route = "collection")
@Composable
fun CollectionScreen(
    viewerId: Int,
    viewerPreferredTitleLang: String,
    viewerViewModel: ViewerViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    navigator: DestinationsNavigator
) {
    // TODO - Currently going back and forth between profile and collection causes it to load the collection again and crashes
    // val viewer  = viewerViewModel.aniListViewer.collectAsState()
    // Log.d("ANILIST", "TEST ${viewer.value.data?.id}")

    // Get initial state of the authenticated user
    LaunchedEffect(Unit) {
        viewerViewModel.setViewerId(viewerId)
        userViewModel.getTsundokuCollection(viewerId, getMediaListSort(viewerPreferredTitleLang)).collect { response ->
            when {
                response is NetworkResource.Success -> {
                    Log.d("Collection Screen", "Instantiating User Collection")
                    val collection: MutableList<TsundokuItem> = mutableListOf()
                    response.data[0]!!.entries!!.fastForEach {
                        val media = it!!.mediaListEntry.media
                        collection.add(
                            TsundokuItem(
                                Media(
                                    mediaId = it.mediaListEntry.mediaId.toString(),
                                    curVolumes = 0,
                                    maxVolumes = 20,
                                    cost = 0.00
                                ),
                                Website.ANILIST,
                                media!!.title!!.userPreferred!!,
                                media.countryOfOrigin.toString(),
                                MediaModel.getCorrectFormat(
                                    media.format!!.name,
                                    media.countryOfOrigin.toString()
                                ),
                                media.chapters ?: 0,
                                it.mediaListEntry.notes ?: "",
                                media.coverImage!!.medium!!
                            )
                        )
                    }
                    viewerViewModel.setTsundokuCollection(collection)
                }
                else -> {
                    Log.d("Collecting Screen", "Failed to get Tsundoku Collection from AniList")
                }
            }
        }
    }

    viewerViewModel.sortTsundokuCollection()
    val viewerUIState by viewerViewModel.viewerState.collectAsState()
    LaunchedEffect(Unit) {
        Log.d("Collection Screen", "Updating Collection Database Data")
        instantiateDatabaseUser(viewerId, viewerViewModel)
        updateDatabaseMediaList(viewerId, viewerUIState.collection!!.toList(), viewerViewModel)
    }

    val collectionUiState by userViewModel.collectionUiState.collectAsState()
    val curEditingMediaIndex = collectionUiState.curEditingMediaIndex
    var selectedPaneIndex by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = Color(0xFF2B2D42),
            ) {
                val items = listOf(
                    BottomNavigationItem(
                        title = "collection",
                        icon = Icons.AutoMirrored.Filled.List,
                        desc = "Collection Pane Nav Button"
                    ),
                    BottomNavigationItem(
                        title = "addseries",
                        icon = Icons.Filled.Add,
                        desc = "Add Series Pane Nav Button"
                    ),
                    BottomNavigationItem(
                        title = "search",
                        icon = Icons.Filled.Face,
                        desc = "Search Pane Nav Button"
                    ),
                    BottomNavigationItem(
                        title = "profile",
                        icon = Icons.Filled.AccountBox,
                        desc = "User Profile Nav Button"
                    )
                )

                items.fastForEachIndexed { index, item ->
                    FloatingActionButton(
                        modifier = Modifier.weight(1f),
                        containerColor = if (selectedPaneIndex == index) TsundokuBackground else Color.Transparent,
                        contentColor = if (selectedPaneIndex == index) Color(0xFF42B1EA) else Color(0xFF777A9E),
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                        onClick = {
                            selectedPaneIndex = index
                            navigator.navigate(item.title)
                        },
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = item.desc
                        )
                    }
                }
            }
        },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TsundokuBackground,
                    titleContentColor = Color.Yellow,
                ),
                title = { },
                navigationIcon = { // TODO - Search field, need to figure out how to set a delay
                    IconButton(onClick = { /* TODO do something */ }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            tint = Color(0xFF42B1EA),
                            contentDescription = "Search Collection Button"

                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO do something */ }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            tint = Color(0xFF42B1EA),
                            contentDescription = "Refresh Collection Button"
                        )
                    }
                    IconButton(onClick = { /* TODO do something */ }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.baseline_filter_list_alt_24),
                            tint = Color(0xFF42B1EA),
                            contentDescription = "Filter Pane Nav Button"
                        )
                    }
                },
            )
        },
    ) {
        Surface (
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            color = Color(0xFF13171D),
        ) {
            Collection(collection = viewerUIState.collection!!.toList(), userViewModel = userViewModel, collectionUiState = collectionUiState)
        }
    }

    if (curEditingMediaIndex > -1) MediaEditScreen(item = viewerUIState.collection!![curEditingMediaIndex], currencySymbol = viewerUIState.currencySymbol, coroutineScope = rememberCoroutineScope(), userViewModel = userViewModel, viewerViewModel = viewerViewModel)
}

@Composable
fun Collection(
    collection: List<TsundokuItem>,
    userViewModel: UserViewModel,
    collectionUiState: CollectionUiState
) {
    Column(
        modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LazyColumn( // TODO - Convert to LazyColumn
            // modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            itemsIndexed(items = collection) { index, item ->
                MediaCard(item = item, index = index, userViewModel, collectionUiState, LocalUriHandler.current)
            }
        }
    }
}

suspend fun instantiateDatabaseUser(viewerId: Int, viewerViewModel: ViewerViewModel) {
    // Get the currency code and check whether the current authenticated user has been onboarded
    val currencyCode = viewerViewModel.getCurrencyCode(viewerId)
    if (currencyCode != null) {
        Log.d("Supabase", "Getting User $viewerId Currency Code")
        viewerViewModel.setCurrencyCode(currencyCode)
        viewerViewModel.setCurrencySymbol(MediaModel.getCurrencySymbol(currencyCode))
    }
    else {
        Log.d("Supabase", "Added new Viewer $viewerId to Database")
        viewerViewModel.insertDatabaseViewer(viewerId)
    }
}

/**
 * Checks the database to see whether the Tsundoku Collection has matching entries in the database for the user, if a media entry is not in the database for the user then we add it
 * @param viewerId Unique id of the authenticated used
 * @param tsundokuCollection The users current Tsundoku Collection from AniList
 */
// TODO - Should I delete entries that are not in AL???
suspend fun updateDatabaseMediaList(viewerId: Int, tsundokuCollection: List<TsundokuItem>, viewerViewModel: ViewerViewModel) {
    val curDbMediaList = viewerViewModel.getDatabaseMediaList(viewerId)
    val insertList: MutableList<Media> = mutableListOf()
    if (curDbMediaList.isEmpty()) {
        tsundokuCollection.forEach {
            insertList.add(Media(viewerId, it.media.mediaId, it.media.curVolumes, it.media.maxVolumes, it.media.cost))
        }
    } else {
        tsundokuCollection.forEach {
            if (!curDbMediaList.parallelStream().anyMatch { media -> media.mediaId == it.media.mediaId }) {
                insertList.add(Media(viewerId, it.media.mediaId, it.media.curVolumes, it.media.maxVolumes, it.media.cost))
            }
        }
    }

    if (insertList.isNotEmpty()) {
        Log.d("Supabase", "Updating Database List for Viewer $viewerId")
        viewerViewModel.insertNewMedia(insertList)
    }
}

