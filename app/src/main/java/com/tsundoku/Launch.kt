package com.tsundoku

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.dependency
import com.tsundoku.anilist.collection.CollectionViewModel
import com.tsundoku.anilist.enums.Lang
import com.tsundoku.anilist.viewer.ViewerViewModel
import com.tsundoku.destinations.CollectionScreenDestination
import com.tsundoku.models.ViewerModel
import com.tsundoku.ui.BottomNavigationBar
import com.tsundoku.ui.LoginScreen
import com.tsundoku.ui.collection.CollectionTopAppBar
import com.tsundoku.ui.loading.LoadingScreen
import com.tsundoku.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class Launch : ComponentActivity() {
    private val viewerViewModel: ViewerViewModel by viewModels()
    private val collectionViewModel: CollectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // TODO - Apply MaterialTheme and use Generator for colors???
        setContent {
            AppTheme (
                darkTheme = true
            ) {
                DisposableEffect(true) {
                    onDispose {
                        runBlocking {
                            ViewerModel.batchUpdateMediaVolumeCount(viewerViewModel, collectionViewModel)
                        }
                        Log.i("Tsundoku", "Closing Tsundoku Android App")
                    }
                }

                val navController = rememberNavController()
                collectionViewModel.onViewer(true)
                Scaffold(
                    bottomBar = { if (viewerViewModel.showBottomAppBar.value) BottomNavigationBar(navController) },
                    topBar = { if (viewerViewModel.showTopAppBar.value) CollectionTopAppBar(viewerViewModel, collectionViewModel) },
                ) {
                    LaunchedEffect(Unit) {
                        viewerViewModel.toggleTopAppBar()
                        viewerViewModel.toggleBottomAppBar()
                    }
                    DestinationsNavHost(
                        navGraph = NavGraphs.root,
                        modifier = androidx.compose.ui.Modifier.padding(it),
                        navController = navController,
                        dependenciesContainerBuilder = {
                            dependency(CollectionScreenDestination) { collectionViewModel }
                            dependency(viewerViewModel)
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        viewerViewModel.onTokenDataReceived(intent?.data)
    }
}

// TODO - Disable rotation
// TODO - Issue where login screen is showing when it shouldn't on initial launch
@Destination("launch")
@RootNavGraph(start = true)
@Composable
fun LaunchPane(
    viewerViewModel: ViewerViewModel,
    navigator: DestinationsNavigator
) {

//    var isLoggedIn = false
//    runBlocking {
//        viewerViewModel.isLoggedIn.collect{
//            isLoggedIn = it
//        }
//    }
    val isLoggedIn by viewerViewModel.isLoggedIn.collectAsState(initial = false)
    Log.d("Tsundoku", "User ${if(isLoggedIn) "is" else "is not"} logged in")
    viewerViewModel.setIsLoading(true)
    if(isLoggedIn) {
        if (viewerViewModel.isLoading.value) LoadingScreen()
        val viewer by viewerViewModel.aniListViewer.collectAsState()
        if (viewer.data != null) {
            Log.d("ANILIST","${viewer.data!!.name} | ${viewer.data!!.id} | ${viewer.data!!.avatar!!.medium} | ${viewer.data!!.bannerImage} | https://anilist.co/user/${viewer.data!!.id}"
            )
            viewerViewModel.setViewerId(viewer.data!!.id)
            viewerViewModel.setPreferredLang(Lang.valueOf(viewer.data!!.options!!.titleLanguage!!.name))
            val customLists by viewerViewModel.getViewerCustomLists(viewer.data!!.id).collectAsState()
            customLists.data?.customLists?.run {
                val customListsOutput = StringBuilder(this.toString().trim())

                // Check to see if the user already has the "Tsundoku" custom list where collection information for this story will be returned
                if (customListsOutput.contains("Tsundoku=")) {
                    Log.d("AniList", "Found Tsundoku List for \"${viewer.data!!.name}\"")
                } else {
                    Log.d("AniList", "Creating Tsundoku Custom List for \"${viewer.data!!.name}\"")
                    viewerViewModel.addTsundokuList(ViewerModel.parseCustomLists(customListsOutput))
                }
            }
            navigator.navigate(CollectionScreenDestination)
        }
        else {
            Log.d("AniList", "Viewer Query returned no data")
        }
    }
    else LoginScreen()
}