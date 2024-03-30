package com.tsundoku.ui

import android.util.Log
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
import androidx.compose.ui.window.Dialog
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
    collectionViewModel: CollectionViewModel
) {
    if (viewerViewModel.showTopAppBar.value) viewerViewModel.turnOffTopAppBar()
    var title: String by rememberSaveable { mutableStateOf("") }
    var curVolumes: String by rememberSaveable { mutableStateOf("0") }
    var maxVolumes: String by rememberSaveable { mutableStateOf("1") }
    var cost: String by rememberSaveable { mutableStateOf("") }
    var isAddButtonEnabled: Boolean by rememberSaveable { mutableStateOf(false) }

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
                    isAddButtonEnabled = it.isNotBlank() && it.toInt() <= maxVolumes.toInt()
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
                    isAddButtonEnabled = it.isNotBlank() && it.toInt() >= curVolumes.toInt()
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
            var openAlertDialog by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current
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

                                            curVolumes = "0"
                                            maxVolumes = "1"
                                            cost = "0.00"

                                            // Add to AniList custom list
                                            viewerViewModel.getMediaCustomLists(it.data.id).collect { getList ->
                                                Log.d("Tsundoku", "Item = null? ${item == null}")
                                                when(getList) {
                                                    is NetworkResource.Success -> {
                                                        Log.d("Tsundoku", "Custom Lists for ${item!!.mediaId} = ${ViewerModel.parseTrueCustomLists(StringBuilder(getList.data.customLists.toString().trim()))}")
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
                                            }
                                        } else {
                                            /* TODO - Create Dialog */
                                            Log.i("Tsundoku", "${item!!.mediaId} Already Exists in Collection")
                                            // Toast.makeText(context, "Already Exists", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    is NetworkResource.Loading -> {  }
                                    else -> {
                                        openAlertDialog = true
                                        Log.i(APP_NAME, "Error! \"$title\" Does not Exist in AniList or Mangadex")
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

            if (openAlertDialog) {
                AlertDialog(
                    onDismissRequest = { openAlertDialog = false },
                    onConfirmation = { openAlertDialog = false },
                    dialogTitle = "Try Again!",
                    dialogText = "\"$title\" Does not Exist",
                    icon = Icons.Default.Info
                )
            }
        }
    }
}

@Composable
fun FormatDropdownMenuItem(
    text: String,
    clickEvent: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(text, fontWeight = FontWeight.ExtraBold, fontFamily = interFont) },
        onClick = { clickEvent },
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

@Composable
fun AddSeriesErrorDialog(

) {
    Dialog(
        onDismissRequest = {  },
    ) {

    }
}