package com.tsundoku.ui

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.tsundoku.APP_NAME
import com.tsundoku.anilist.collection.CollectionViewModel
import com.tsundoku.anilist.viewer.ViewerViewModel
import com.tsundoku.data.NetworkResource
import com.tsundoku.interFont
import kotlinx.coroutines.launch

@Destination(route = "user-search")
@Composable
fun UserSearchScreen(
    viewerViewModel: ViewerViewModel,
    collectionViewModel: CollectionViewModel,
    navigator: DestinationsNavigator
) {
    if (viewerViewModel.showTopAppBar.value == true) viewerViewModel.turnOffTopAppBar()
    var username: String by remember { mutableStateOf("") }
    var openInvalidSearchedUserDialog: Boolean by remember { mutableStateOf(false) }
    var invalidSearchedUserDialogTitle: String by remember { mutableStateOf("") }

    if (openInvalidSearchedUserDialog) {
        AlertDialog(
            dialogTitle = invalidSearchedUserDialogTitle,
            dialogText = "\"$username\"",
            icon = Icons.Default.Info,
            onDismissRequest = { openInvalidSearchedUserDialog = false },
            onConfirmation = { openInvalidSearchedUserDialog = false },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF13171D))
            .padding(20.dp, 100.dp, 20.dp, 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username", color = Color(0xFFC8C9E4), fontWeight = FontWeight.ExtraBold) },
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFont
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color(0xFF42B1EA),
                unfocusedBorderColor = Color(0xFF42B1EA),
                unfocusedContainerColor = Color(0xFF1F232D),
                unfocusedLabelColor = Color(0xFF9EAEBD),
                focusedTextColor = Color(0xFFC8C9E4),
                focusedContainerColor = Color(0xFF1F232D),
            )
        )

        val coroutineScope = rememberCoroutineScope()
        val controller = LocalSoftwareKeyboardController.current
        ElevatedButton(
            onClick = {
                coroutineScope.launch {
                    collectionViewModel.validateSearchedUser(username).collect {
                        when (it) {
                            is NetworkResource.Success -> {
                                val customListsOutput = it.data.mediaListOptions!!.mangaList?.customLists.toString().trim()
                                if (customListsOutput.contains(APP_NAME)) {
                                    Log.d("AniList", "Found Tsundoku List for Searched User \"$username\"")
                                    controller?.hide()
                                    collectionViewModel.isSuccessfulUserSearch(true)
                                    collectionViewModel.setSearchedUser(it.data)
                                    viewerViewModel.setIsLoading(true)
                                    viewerViewModel.turnOffAppBar()
                                    viewerViewModel.setSelectedScreenIndex(0)
                                    navigator.navigate("collection")
                                } else {
                                    Log.d("AniList", "User \"$username\" Does not use Tsundoku")
                                    invalidSearchedUserDialogTitle = "Does not use Tsundoku!"
                                    openInvalidSearchedUserDialog = true
                                }
                            }
                            is NetworkResource.Error -> {
                                Log.e("AniList", "User Query returned no data")
                                invalidSearchedUserDialogTitle = "Is Not a AniList User"
                                openInvalidSearchedUserDialog = true
                            }
                            is NetworkResource.Loading -> Log.d(APP_NAME, "Loading Searched User \"$username\" Data")
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .width(20.dp),
            border = BorderStroke(width = 2.dp, color = Color(0xFF42B1EA)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF191D26),
                contentColor = Color(0xFF42B1EA)
            ),
            shape = RoundedCornerShape(8),
            elevation =  ButtonDefaults.buttonElevation(
                defaultElevation = 10.dp,
                pressedElevation = 15.dp,
                disabledElevation = 0.dp,

                ),
            content = {
                Text(
                    text = "Search",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp)
            }
        )
    }
}