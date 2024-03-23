package com.tsundoku

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.tsundoku.anilist.viewer.ViewerViewModel
import com.tsundoku.data.CleanData
import com.tsundoku.destinations.CollectionScreenDestination
import com.tsundoku.ui.LoginScreen
import com.tsundoku.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Launch : ComponentActivity() {
    private val viewerViewModel: ViewerViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // TODO - Apply MaterialTheme and use Generator for colors
        setContent {
            AppTheme (
                darkTheme = true
            ) {
                val navController = rememberNavController()
//                val navHostEngine = rememberNavHostEngine(
//                    rootDefaultAnimations = RootNavGraphDefaultAnimations(
//                        enterTransition = { fadeIn(animationSpec = tween(1000)) },
//                        exitTransition = { fadeOut(animationSpec = tween(300)) },
//                    )
//                )

//                val items = listOf(
//                    BottomNavigationItem(
//                        title = "collection",
//                        icon = Icons.AutoMirrored.Filled.List,
//                        desc = "Collection Pane Nav Button"
//                    ),
//                    BottomNavigationItem(
//                        title = "addseries",
//                        icon = Icons.Filled.Add,
//                        desc = "Add Series Pane Nav Button"
//                    ),
//                    BottomNavigationItem(
//                        title = "search",
//                        icon = Icons.Filled.Face,
//                        desc = "Search Pane Nav Button"
//                    ),
//                    BottomNavigationItem(
//                        title = "profile",
//                        icon = Icons.Filled.AccountBox,
//                        desc = "User Profile Nav Button"
//                    )
//                )

                // var selectedPaneIndex by rememberSaveable { mutableIntStateOf(0) }
                DestinationsNavHost(
                    navGraph = NavGraphs.root,
                    navController = navController
                )
//                Scaffold(
//                    bottomBar = {
//                        NavigationBar(  // TODO - Change to dif bar type
//                            containerColor = Color(0xFF2B2D42)
//                        ) {
//                            items.fastForEachIndexed { index, item ->
//                                IconButton(
//                                    onClick = {
//                                        selectedScreenIndex = index
//                                        navController.navigate(item.title)
//                                    },
//                                    modifier = Modifier.weight(1f)) {
//                                    Icon(item.selectedIcon,
//                                        contentDescription = "",
//                                        modifier = Modifier.size(40.dp),
//                                        //tint = if (selectedScreenIndex == index) AniListBlue else AniListGrey
//                                    )
//                                }
//                            }
//                        }
//                    }
//                ) {
//                    DestinationsNavHost(
//                        navGraph = NavGraphs.root,
//                        modifier = Modifier.padding(it),
//                        navController = navController
//                    )
//                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        viewerViewModel.onTokenDataReceived(intent?.data)
    }
}

// TODO - Fix Login screen
// TODO - Disable rotation
// TODO - Refactor this so it doesn't create the nav until user is logged in
// TODO - Add loading collection screen
@RootNavGraph(start = true)
@Destination(route = "launch")
@Composable
fun LaunchPane(
    navigator: DestinationsNavigator,
    viewerViewModel: ViewerViewModel = hiltViewModel()
) {
    val isLoggedIn by viewerViewModel.isLoggedIn.collectAsState(initial = false)
    Log.d("ANILIST", "User ${if(isLoggedIn) "is" else "is not"} logged in")

    if (isLoggedIn) {
        val viewer by viewerViewModel.aniListViewer.collectAsState()
        if (viewer.data != null) {
            // Log.d("ANILIST", "${viewer.data!!.name} | ${viewer.data!!.id} | ${viewer.data!!.avatar!!.medium} | ${viewer.data!!.bannerImage} | https://anilist.co/user/${viewer.data!!.id}")
            val customLists by viewerViewModel.getCustomLists(viewer.data!!.id).collectAsState()
            customLists.data?.customLists?.run {
                val customListsOutput = StringBuilder(this.toString().trim())

                // Check to see if the user already has the "Tsundoku" custom list where collection information for this story will be returned
                if (customListsOutput.contains("Tsundoku=")) {
                    Log.d("ANILIST", "Found Tsundoku List for \"${viewer.data!!.name}\"")
                }
                else {
                    Log.d("ANILIST", "Creating Tsundoku Custom List for \"${viewer.data!!.name}\"")
                    viewerViewModel.addTsundokuList(CleanData.parseCustomLists(customListsOutput))
                }
            }
            // CollectionScreenDestination.NavArgs(viewer.data!!.id, viewer.data!!.options!!.titleLanguage!!.name)
            navigator.navigate(CollectionScreenDestination(viewer.data!!.id, viewer.data!!.options!!.titleLanguage!!.name))
        }
        else {
            Log.d("ANILIST", "Viewer Query returned no data")
        }
    }
    else {
        LoginScreen()
    }
}