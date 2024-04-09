package com.tsundoku

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.dependency
import com.tsundoku.anilist.collection.CollectionViewModel
import com.tsundoku.anilist.enums.Lang
import com.tsundoku.anilist.viewer.ViewerViewModel
import com.tsundoku.data.NetworkResource
import com.tsundoku.destinations.AddMediaScreenDestination
import com.tsundoku.destinations.CollectionScreenDestination
import com.tsundoku.extensions.ContextExt.getActivity
import com.tsundoku.extensions.firstBlocking
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
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // CookieExt.setup()

        // TODO - Apply MaterialTheme and use Generator for colors???
        setContent {
            AppTheme (
                darkTheme = true
            ) {
                LocalContext.current.getActivity()?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                DisposableEffect(true) {
                    onDispose {
                        runBlocking {
                            ViewerModel.batchUpdateMediaVolumeCount(viewerViewModel, collectionViewModel)
                        }
                        Log.i(APP_NAME, "Closing Tsundoku Android App")
                    }
                }

                val navController = rememberNavController()
                collectionViewModel.onViewer(true)
                Scaffold(
                    bottomBar = { if (viewerViewModel.showBottomAppBar.value) BottomNavigationBar(viewerViewModel, navController) },
                    topBar = { if (viewerViewModel.showTopAppBar.value) CollectionTopAppBar(viewerViewModel, collectionViewModel) },
                    contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
                ) {
                    DestinationsNavHost(
                        navGraph = NavGraphs.root,
                        modifier = Modifier.padding(it),
                        navController = navController,
                        dependenciesContainerBuilder = {
                            dependency(CollectionScreenDestination) { collectionViewModel }
                            dependency(AddMediaScreenDestination) { collectionViewModel }
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

@Destination("launch")
@RootNavGraph(start = true)
@Composable
fun LaunchPane(
    viewerViewModel: ViewerViewModel,
    navigator: DestinationsNavigator
) {
    LaunchedEffect(Unit) {
        viewerViewModel.setIsLoading(true)
        viewerViewModel.turnOffAppBar()
    }

    val isLoggedIn by viewerViewModel.isLoggedIn.collectAsStateWithLifecycle(viewerViewModel.isLoggedIn.firstBlocking())
    Log.d("TEST", "LOGGED IN =? $isLoggedIn")
    when {
        isLoggedIn -> {
            if (viewerViewModel.isLoading.value) LoadingScreen()
            val viewer by viewerViewModel.aniListViewer.collectAsState()
            when (viewer) {
                is NetworkResource.Success -> {
                    viewer.data!!.run viewer@ {
                        viewerViewModel.setViewerData(this@viewer)
                        Log.d("AniList","${this@viewer.name} | ${this@viewer.id} | ${this@viewer.avatar!!.medium} | ${this@viewer.bannerImage} | https://anilist.co/user/${this@viewer.id}"
                        )
                        viewerViewModel.setViewerId(this@viewer.id)
                        viewerViewModel.setPreferredLang(Lang.valueOf(this@viewer.options!!.titleLanguage!!.name))
                        val customLists by viewerViewModel.getViewerCustomLists(this.id).collectAsState()
                        customLists.data?.customLists?.run customList@ {
                            val customListsOutput = StringBuilder(this@customList.toString().trim())

                            // Check to see if the user already has the "Tsundoku" custom list where collection information for this story will be returned
                            if (customListsOutput.contains("Tsundoku=")) {
                                Log.d("AniList", "Found Tsundoku List for \"${this@viewer.name}\"")
                            } else {
                                Log.d("AniList", "Creating Tsundoku Custom List for \"${this@viewer.name}\"")
                                viewerViewModel.addTsundokuList(ViewerModel.parseCustomLists(customListsOutput))
                            }
                        }
                        navigator.navigate(CollectionScreenDestination)
                    }
                }
                is NetworkResource.Error -> Log.e("AniList", "Viewer Query returned no data")
                is NetworkResource.Loading -> Log.d("Tsundoku", "Loading Viewer Data")
            }
        }
        else -> {
            Log.d("Tsundoku", "Returning to Login Screen")
            LoginScreen()
        }
    }
}