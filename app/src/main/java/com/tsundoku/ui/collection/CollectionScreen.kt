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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.tsundoku.GetTsundokuCollectionQuery
import com.tsundoku.R
import com.tsundoku.anilist.user.UserViewModel
import com.tsundoku.anilist.viewer.ViewerViewModel
import com.tsundoku.data.NetworkResource
import com.tsundoku.ui.BottomNavigationItem
import com.tsundoku.ui.model.CollectionUiState
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
    val tsundokuCollection = remember { userViewModel.getTsundokuCollection(viewerId, getMediaListSort(viewerPreferredTitleLang)) }.collectAsState()
    val collectionUiState by userViewModel.collectionUiState.collectAsState()
    val curEditingMediaIndex = collectionUiState.curEditingMediaIndex
    when {
        tsundokuCollection.value is NetworkResource.Success -> {
            var selectedPaneIndex by rememberSaveable { mutableIntStateOf(0) }
            val collection = tsundokuCollection.value.data?.get(0)?.entries
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
                    Collection(collection = collection, userViewModel = userViewModel, collectionUiState = collectionUiState)
                }
            }
            if (curEditingMediaIndex > -1) MediaEditScreen(entry = collection!![curEditingMediaIndex]!!.mediaListEntry, userViewModel = userViewModel, viewerViewModel = viewerViewModel)
        }
        else -> {
            Log.d("ANILIST", "LOADING USER TSUNDOKU COLLECTION")
        }
    }
}

@Composable
fun Collection(
    collection: List<GetTsundokuCollectionQuery.Entry?>?,
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
            itemsIndexed(items = collection!!) { index, entry ->
                MediaCard(entry = entry!!, index = index, userViewModel, collectionUiState, LocalUriHandler.current)
            }
        }
    }
}

