package com.tsundoku.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.tsundoku.anilist.viewer.ViewerViewModel
import com.tsundoku.ui.theme.TsundokuBackground

@Composable
fun BottomNavigationBar(
    viewerViewModel: ViewerViewModel,
    navController: NavController
) {
    val viewerState by viewerViewModel.viewerState.collectAsState()
    val selectedScreenIndex = viewerState.selectedScreenIndex
    val items = listOf(
        BottomNavigationItem(
            path = "collection",
            title = "Home",
            icon = Icons.AutoMirrored.Filled.List,
            desc = "Collection Pane Nav Button"
        ),
        BottomNavigationItem(
            path = "addmedia",
            title = "Add",
            icon = Icons.Filled.Add,
            desc = "Add Series Pane Nav Button"
        ),
        BottomNavigationItem(
            path = "user-search",
            title = "Search",
            icon = Icons.Filled.Face,
            desc = "Search Pane Nav Button"
        ),
        BottomNavigationItem(
            path = "profile",
            title = "Profile",
            icon = Icons.Filled.AccountBox,
            desc = "User Profile Nav Button"
        )
    )

    BottomAppBar(
        containerColor = Color(0xFF2B2D42),
    ) {
        items.forEachIndexed { index, item ->
            FloatingActionButton(
                modifier = Modifier.weight(1f),
                containerColor = if (selectedScreenIndex == index) TsundokuBackground else Color.Transparent,
                contentColor = if (selectedScreenIndex == index) Color(0xFF42B1EA) else Color(0xFF777A9E),
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(),
                onClick = {
                    viewerViewModel.setSelectedScreenIndex(index)
                    navController.navigate(item.path)
                    if(item.path != "collection") viewerViewModel.turnOffTopAppBar()
                },
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        item.icon,
                        contentDescription = item.desc
                    )
                    Text(
                        text = item.title
                    )
                }
            }
        }
    }
}

data class BottomNavigationItem(
    val path: String,
    val title: String,
    val icon: ImageVector,
    val desc: String
)