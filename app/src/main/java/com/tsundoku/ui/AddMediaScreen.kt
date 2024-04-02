package com.tsundoku.ui

import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import com.tsundoku.APP_NAME
import com.tsundoku.anilist.collection.CollectionViewModel
import com.tsundoku.anilist.viewer.ViewerViewModel
import com.tsundoku.data.NetworkResource
import com.tsundoku.data.TsundokuFormat
import com.tsundoku.interFont
import com.tsundoku.models.Media
import com.tsundoku.models.MediaModel
import com.tsundoku.models.TsundokuItem
import com.tsundoku.models.ViewerModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Destination(route = "addmedia")
@Composable
fun AddMediaScreen(
    viewerViewModel: ViewerViewModel,
    collectionViewModel: CollectionViewModel,
) {
    if (viewerViewModel.showTopAppBar.value) viewerViewModel.turnOffTopAppBar()
    var title: String by rememberSaveable { mutableStateOf("") }
    var curVolumes: String by rememberSaveable { mutableStateOf("") }
    var maxVolumes: String by rememberSaveable { mutableStateOf("") }
    var cost: String by rememberSaveable { mutableStateOf("") }
    var isAddButtonEnabled: Boolean by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF13171D))
            .padding(20.dp, 20.dp, 20.dp, 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TextField(
            value = title,
            onValueChange = {
                title = it
                isAddButtonEnabled = it.isNotEmpty()
            },
            label = {
                Text(
                    "Title or ID",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    fontFamily = interFont
                )
            },
            placeholder = { Text("Enter Title or ID...") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 20.sp,
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
            ),
            maxLines = 16,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )
        TextField(
            value = cost,
            leadingIcon = {
                Text(
                    viewerViewModel.getCurrencySymbol(),
                    color = Color(0xFFC8C9E4),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    fontFamily = interFont
                ) },
            onValueChange = {
                if (it.isEmpty() || (it.length <= 25 && MediaModel.costRegex.matches(it))) cost = it
                isAddButtonEnabled = it.isNotBlank()
             },
            label = {
                Text(
                "Cost",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    fontFamily = interFont
                )
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 20.sp,
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
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Row(
            modifier = Modifier.padding(0.dp, 5.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextField(
                value = curVolumes,
                onValueChange = {
                    if (MediaModel.volumeNumRegex.matches(it)) curVolumes = it
                    isAddButtonEnabled = if (it.isNotBlank() && maxVolumes.isNotBlank()) (it.toInt() <= maxVolumes.toInt()) else false
                },
                label = { Text("Cur Volumes", color = Color(0xFFC8C9E4), fontWeight = FontWeight.ExtraBold) },
                modifier = Modifier
                    .height(60.dp)
                    .weight(1f),
                textStyle = TextStyle(
                    fontSize = 20.sp,
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
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            TextField(
                value = maxVolumes,
                onValueChange = {
                    if (MediaModel.volumeNumRegex.matches(it)) maxVolumes = it
                    isAddButtonEnabled = if(it.isNotBlank() && curVolumes.isNotBlank()) (it.toInt() >= curVolumes.toInt()) else false
                },
                label = { Text("Max Volumes", color = Color(0xFFC8C9E4), fontWeight = FontWeight.ExtraBold) },
                modifier = Modifier
                    .height(60.dp)
                    .weight(1f),
                textStyle = TextStyle(
                    fontSize = 20.sp,
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
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var expanded by remember { mutableStateOf(false) }
            var selectedFormat by remember { mutableStateOf("Manga") }
            Box(
                modifier = Modifier.weight(0.5f)
            ) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = selectedFormat,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = Color(0xFF42B1EA),
                            unfocusedBorderColor = Color(0xFF42B1EA),
                            unfocusedContainerColor = Color(0xFF1F232D),
                            unfocusedLabelColor = Color(0xFF9EAEBD),
                            focusedTextColor = Color(0xFFC8C9E4),
                            focusedContainerColor = Color(0xFF1F232D),
                        ),
                        textStyle = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = interFont,
                            color = Color(0xFF9EAEBD)
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(0xFF13171D)),
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "Manga",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = interFont,
                                    color = if(selectedFormat == "Manga") Color(0xFF42B1EA) else Color(0xFF9EAEBD)
                                )
                            },
                            onClick = {
                                selectedFormat = "Manga"
                                expanded = false
                            },
                            colors = MenuItemColors(
                                textColor = Color(0xFFC8C9E4),
                                leadingIconColor = Color(0xFF42B1EA),
                                trailingIconColor = Color(0xFF42B1EA),
                                disabledLeadingIconColor = Color.Transparent,
                                disabledTextColor = Color.Transparent,
                                disabledTrailingIconColor = Color.Transparent
                            )
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                "Novel",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = interFont,
                                    color = if(selectedFormat == "Novel") Color(0xFF42B1EA) else Color(0xFF9EAEBD)
                                )
                            },
                            onClick = {
                                selectedFormat = "Novel"
                                expanded = false
                            },
                            colors = MenuItemColors(
                                textColor = Color(0xFFC8C9E4),
                                leadingIconColor = Color(0xFF42B1EA),
                                trailingIconColor = Color(0xFF42B1EA),
                                disabledLeadingIconColor = Color.Transparent,
                                disabledTextColor = Color.Transparent,
                                disabledTrailingIconColor = Color.Transparent
                            )
                        )
                    }
                }
            }
            var openTryAgainDialog by remember { mutableStateOf(false) }
            var openAlreadyExistsDialog by remember { mutableStateOf(false) }
            var showSuccessToast by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            OutlinedButton(
                modifier = Modifier
                    .weight(0.5f),
                enabled = isAddButtonEnabled,
                onClick = {
                    val format = if(selectedFormat == "Manga") TsundokuFormat.MANGA else TsundokuFormat.NOVEL

                    if (MediaModel.validateUUIDRegex.matches(title)) {
                        Log.i(APP_NAME, "Adding $title | $format | $curVolumes | $maxVolumes | $cost to Mangadex")
                    } else {
                        val aniListId = title.trim().toIntOrNull()
                        val curVolumesInt = curVolumes.trim('0').toIntOrNull() ?: 0
                        val maxVolumesInt = maxVolumes.trim('0').toIntOrNull() ?: 1
                        val costVal = cost.trim('0').toBigDecimalOrNull() ?: BigDecimal(0.00)

                        coroutineScope.launch(Dispatchers.IO) {
                            var item: TsundokuItem?
                            viewerViewModel.getNewAniListMediaSeries(seriesId = aniListId, title = if(aniListId == null) title else null, format = format).collect { it ->
                                when(it) {
                                    is NetworkResource.Success -> {
                                        item = MediaModel.parseAniListMedia(it.data, curVolumesInt, maxVolumesInt, costVal)
                                        Log.i("Tsundoku", "Successfully got $item from AniList")

                                        if (!collectionViewModel.tsundokuCollection.value.any { curItem -> curItem.mediaId == item!!.mediaId }) {
                                            // Add to database
                                            viewerViewModel.insertNewDatabaseMedia(listOf(Media(viewerViewModel.getViewerId(), item!!.mediaId, curVolumesInt, maxVolumesInt, costVal)))

                                            // Add to list
                                            collectionViewModel.addItemToTsundokuCollection(item!!)

                                            // Reset text fields to empty
                                            curVolumes = ""
                                            maxVolumes = ""
                                            cost = ""

                                            // Add to AniList custom list
                                            viewerViewModel.getMediaCustomLists(it.data.id).collect { getList ->
                                                when(getList) {
                                                    is NetworkResource.Success -> {
                                                        Log.d("Tsundoku", "Custom Lists for ${item!!.mediaId} is ${ViewerModel.parseTrueCustomLists(StringBuilder(getList.data.customLists.toString().trim()))}")
                                                        item?.mediaId?.toInt()?.let { itItem ->
                                                            viewerViewModel.addAniListMediaToCollection(itItem, ViewerModel.parseTrueCustomLists(StringBuilder(getList.data.customLists.toString().trim())))
                                                        }
                                                    }
                                                    else -> {
                                                        Log.d("Tsundoku", "Custom Lists Empty for ${item!!.mediaId}")
                                                        item?.mediaId?.toInt()?.let { itItem ->
                                                            viewerViewModel.addAniListMediaToCollection(itItem,  mutableListOf())
                                                        }
                                                    }
                                                }
                                                showSuccessToast = true
                                            }
                                        } else {
                                            openAlreadyExistsDialog = true
                                            Log.w("Tsundoku", "\"$title\" Already Exists in Collection")
                                        }
                                    }
                                    is NetworkResource.Loading -> {  }
                                    else -> {
                                        openTryAgainDialog = true
                                        Log.w(APP_NAME, "\"$title\" Does not Exist in AniList or Mangadex")
                                    }
                                }
                            }
                        }
                    }
                },
                colors = ButtonColors(
                    containerColor = Color(0xFF1F232D),
                    contentColor = Color(0xFFC8C9E4),
                    disabledContainerColor = Color(0xFFC8C9E4),
                    disabledContentColor = Color(0xFF1F232D),
                ),
            ) {
                Text(
                    "Add Series",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFont
                )
            }

            if (openTryAgainDialog) {
                AlertDialog(
                    dialogTitle = "Does not Exist!",
                    dialogText = "\"$title\"",
                    icon = Icons.Default.Info,
                    onDismissRequest = { openTryAgainDialog = false },
                    onConfirmation = { openTryAgainDialog = false },
                )
            }
            else if(openAlreadyExistsDialog) {
                AlertDialog(
                    dialogTitle = "Already Added!",
                    dialogText = "\"$title\"",
                    icon = Icons.Default.Info,
                    onDismissRequest = { openAlreadyExistsDialog = false },
                    onConfirmation = { openAlreadyExistsDialog = false },
                )
            }
            else if(showSuccessToast) {
                val toast = Toast.makeText(context, "\"$title\" Added", Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
                toast.show()
                title = ""
                showSuccessToast = false
            }
            else title = ""
        }
    }
}