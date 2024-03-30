package com.tsundoku.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.fastForEachIndexed
import androidx.navigation.NavController
import com.tsundoku.anilist.viewer.ViewerViewModel
import com.tsundoku.data.BottomNavigationItem
import com.tsundoku.ui.theme.TsundokuBackground

@Composable
fun BottomNavigationBar(
    viewerViewModel: ViewerViewModel,
    navController: NavController
) {
    var selectedPaneIndex by rememberSaveable { mutableIntStateOf(0) }
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
                title = "addmedia",
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
                    navController.navigate(item.title)
                },
            ) {
                Icon(
                    item.icon,
                    contentDescription = item.desc
                )
            }
        }
    }
}