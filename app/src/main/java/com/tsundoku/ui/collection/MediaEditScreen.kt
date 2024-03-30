package com.tsundoku.ui.collection

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tsundoku.R
import com.tsundoku.anilist.viewer.ViewerViewModel
import com.tsundoku.interFont
import com.tsundoku.models.MediaModel
import com.tsundoku.models.TsundokuItem
import com.tsundoku.models.Website
import com.tsundoku.type.MediaListSort
import com.tsundoku.ui.theme.TextColorPrimary
import com.tsundoku.ui.theme.TextColorSecondary
import com.tsundoku.ui.theme.TsundokuBackgroundFade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO - Fix styling
/**
 * The screen that allows users to update information about a particular series
 * @param item The TsundokuItem containing all of the card data
 */
@Composable
fun MediaEditScreen(
    item: TsundokuItem,
    coroutineScope: CoroutineScope,
    viewerViewModel: ViewerViewModel
) {
    var notes: String by rememberSaveable { mutableStateOf(item.notes) }
    var cost: String by rememberSaveable { mutableStateOf(item.cost.toString()) }
    var saveEnabled: Boolean by rememberSaveable { mutableStateOf(true) }
    var curVolumes: String by rememberSaveable { mutableStateOf(item.curVolumes.value) }
    var maxVolumes: String by rememberSaveable { mutableStateOf(item.maxVolumes.value) }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF13171D))
            .padding(20.dp, 20.dp, 20.dp, 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                ImageVector.vectorResource(id = R.drawable.baseline_menu_book_24),
                contentDescription = "Media Format Icon",
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF777A9E)
            )
            // TODO - Need to add a little padding top align vertically
            Text(
                text = "${item.format} | ${item.status}",
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .padding(5.dp, 5.dp, 0.dp, 0.dp),
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = TextColorPrimary,
                fontFamily = interFont
            )
        }
        // TODO - Fix padding issue, needs to be centered
        Text(
            text = item.title,
            modifier = Modifier.height(IntrinsicSize.Min),
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            color = TextColorSecondary,
            overflow = TextOverflow.Ellipsis,
            fontFamily = interFont
        )
        Row(
            modifier = Modifier.padding(0.dp, 5.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = item.curVolumes.value,
                onValueChange = {
                    if (MediaModel.volumeNumRegex.matches(it)) item.curVolumes.value = it
                    saveEnabled = !(it.isBlank() || item.maxVolumes.value.isBlank() || it.toInt() > item.maxVolumes.value.toInt())
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
            OutlinedTextField(
                value = item.maxVolumes.value,
                onValueChange = {
                    if (MediaModel.volumeNumRegex.matches(it)) item.maxVolumes.value = it
                    saveEnabled = !(it.isBlank() || item.curVolumes.value.isBlank() || it.toInt() < item.curVolumes.value.toInt())
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
        OutlinedTextField(
            value = cost,
            leadingIcon = { Text(viewerViewModel.getCurrencySymbol(), color = Color(0xFFC8C9E4), fontWeight = FontWeight.ExtraBold) },
            onValueChange = { if (it.isEmpty() || (it.length <= 25 && MediaModel.costRegex.matches(it))) cost = it },
            label = { Text("Cost") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 5.dp),
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
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes", color = Color(0xFFC8C9E4), fontWeight = FontWeight.ExtraBold) },
            modifier = Modifier
                .padding(0.dp, 5.dp, 0.dp, 10.dp)
                .weight(1f)
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 5.dp, 0.dp, 10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val context = LocalContext.current
            OutlinedButton(
                colors = ButtonColors(
                    containerColor = Color(0xFF1F232D),
                    contentColor = Color(0xFFC8C9E4),
                    disabledContainerColor = Color(0xFFC8C9E4),
                    disabledContentColor = Color(0xFF1F232D),
                ),
                modifier = Modifier
                    .border(2.dp, Color(0xFFC8C9E4), RoundedCornerShape(16.dp))
                    .height(35.dp)
                    .width(110.dp),
                enabled = saveEnabled,
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        val updateMap = mutableMapOf<String, Any?>()
                        if(item.notes != notes) {
                            item.notes = notes
                            when(item.website) {
                                Website.ANILIST -> viewerViewModel.updateAniListMediaNotes(item.mediaId, notes)
                                Website.MANGADEX -> { updateMap["notes"] = notes }
                            }
                        }

                        val maxVolumesInt = item.maxVolumes.value.toInt()
                        val curVolumesInt = item.curVolumes.value.toInt()
                        if(curVolumes.toInt() != curVolumesInt) {
                            if (curVolumesInt <= maxVolumesInt) {
                                Log.d("Supabase", "Updating Cur Volumes for ${item.title}")
                                item.curVolumes.value = curVolumesInt.toString()
                                updateMap["curVolumes"] = curVolumesInt
                            } else {
                                Toast.makeText(context, "Cur Volumes > Max Volumes", Toast.LENGTH_SHORT).show()
                            }
                        }
                        if(maxVolumes.toInt() != maxVolumesInt) {
                            if (maxVolumesInt >= curVolumesInt) {
                                Log.d("Supabase", "Updating Max Volumes for ${item.title}")
                                item.maxVolumes.value = maxVolumesInt.toString()
                                updateMap["maxVolumes"] = maxVolumesInt
                            } else {
                                Toast.makeText(context, "Max Volumes < Cur Volumes", Toast.LENGTH_SHORT).show()
                            }
                        }

                        if(item.cost != cost.toBigDecimal()) {
                            Log.d("Supabase", "Updating Cost for ${item.title}")
                            item.cost = cost.toBigDecimal()
                            updateMap["cost"] = cost
                        }

                        if (updateMap.isNotEmpty()) {
                            viewerViewModel.updateDatabaseMedia(viewerViewModel.getViewerId(), item.mediaId, updateMap)
                        }
                    }
                },
            ) {
                Text(
                    "Save",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFont
                )
            }
            OutlinedButton(
                colors = ButtonColors(
                    containerColor = Color(0xFF1F232D),
                    contentColor = Color(0xFFC8C9E4),
                    disabledContainerColor = Color(0xFFC8C9E4),
                    disabledContentColor = Color(0xFF1F232D),
                ),
                modifier = Modifier
                    .border(2.dp, Color(0xFFC8C9E4), RoundedCornerShape(16.dp))
                    .height(35.dp)
                    .width(110.dp),
                onClick = {
                    viewerViewModel.turnOnTopAppBar()
                    viewerViewModel.setSelectedItemIndex(-1)
                },
            ) {
                Text(
                    "Return",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontFamily = interFont
                )
            }
        }
    }
}

/**
 * Gets the correct sort based on the users preferred title lang
 * @param lang The users preferred title display lang
 */
fun getMediaListSort(lang: String): List<MediaListSort> {
    return when (lang) {
        "ENGLISH" -> listOf(MediaListSort.MEDIA_TITLE_ENGLISH)
        "NATIVE" -> listOf(MediaListSort.MEDIA_TITLE_NATIVE)
        else -> listOf(MediaListSort.MEDIA_TITLE_ROMAJI)
    }
}

@Preview
@Composable
fun MediaEditScreenPreview() {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(TsundokuBackgroundFade)
            .padding(20.dp, 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                ImageVector.vectorResource(id = R.drawable.baseline_menu_book_24),
                contentDescription = "Media Format Icon",
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF777A9E)
            )
            // TODO - Need to add a little padding top align vertically
            Text(
                text = "Manga",
                modifier = Modifier
                    .weight(1f)
                    .padding(5.dp, 5.dp, 0.dp, 0.dp),
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = TextColorPrimary,
                fontFamily = interFont
            )
        }
        Text(
            text = "Lorem ipsum dolor sit amet",
            modifier = Modifier.height(IntrinsicSize.Min),
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            color = TextColorSecondary,
            overflow = TextOverflow.Ellipsis,
            fontFamily = interFont
        )
        OutlinedTextField(
            value = "500.00",
            leadingIcon = { Text("$") },
            onValueChange = {  },
            label = { Text("Cost") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 5.dp, 0.dp, 10.dp),
            textStyle = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFont
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color(0xFF42B1EA),
                unfocusedBorderColor = Color(0xFF42B1EA),
                unfocusedContainerColor = Color(0xFF151F2E),
                unfocusedLabelColor = Color(0xFF9EAEBD),
                focusedTextColor = Color(0xFFC8C9E4),
                focusedContainerColor = Color(0xFF151F2E)
            ),
            singleLine = true
        )
        Row(
            //modifier = Modifier.padding(0.dp, 5.dp, 0.dp, 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = "150",
                onValueChange = {  },
                label = { Text("Cur Volumes") },
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
                    unfocusedContainerColor = Color(0xFF151F2E),
                    unfocusedLabelColor = Color(0xFF9EAEBD),
                    focusedTextColor = Color(0xFFC8C9E4),
                    focusedContainerColor = Color(0xFF151F2E)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = "200",
                onValueChange = {  },
                label = { Text("Max Volumes") },
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
                    unfocusedContainerColor = Color(0xFF151F2E),
                    unfocusedLabelColor = Color(0xFF9EAEBD),
                    focusedTextColor = Color(0xFFC8C9E4),
                    focusedContainerColor = Color(0xFF151F2E)
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        OutlinedTextField(
            value = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec ornare dapibus porta. Nam consequat dictum massa, eu egestas lectus malesuada vitae. Phasellus hendrerit lacus non varius vulputate. Quisque vitae lacus eleifend, fermentum ante at, consequat nisl. Curabitur eget turpis tristique quam egestas tincidunt. Sed in odio vel quam convallis auctor a vel nisi. Curabitur a mollis velit. Morbi hendrerit tristique diam, vitae dictum mauris egestas sed. Aenean nec fringilla arcu. Vivamus tristique metus at ornare condimentum. Vivamus vitae erat eget tortor viverra semper nec vitae dui. Nunc auctor pharetra ullamcorper. Proin auctor laoreet libero, nec laoreet nunc interdum sit amet.\n" +
                    "\n" +
                    "Praesent a sapien vel dolor gravida mollis. Aliquam vitae tellus vel ex venenatis suscipit. Donec vitae massa eu ligula pulvinar vestibulum. Etiam quis erat iaculis, auctor sapien ut, rutrum quam. Praesent euismod condimentum risus, et malesuada odio dignissim eget. Sed urna diam, feugiat eget tristique at, rutrum at arcu. Mauris imperdiet erat ut risus bibendum dapibus. Fusce hendrerit, neque sed gravida dignissim, augue justo gravida leo, egestas accumsan nisl mauris at arcu. Maecenas dictum diam ut nulla lacinia finibus. Sed vitae libero turpis. Integer tortor ante, dignissim nec eleifend sit amet, maximus sit amet neque. Donec quis nunc vitae lectus tempus eleifend in quis diam. Aliquam elit est, pharetra nec imperdiet sit amet, convallis nec augue.",
            onValueChange = {  },
            label = { Text("Notes", fontWeight = FontWeight.Bold) },
            modifier = Modifier
                .padding(0.dp, 5.dp, 0.dp, 10.dp)
                .weight(1f)
                .fillMaxWidth(),
            //.verticalScroll(rememberScrollState()),
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = interFont
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color(0xFF42B1EA),
                unfocusedBorderColor = Color(0xFF42B1EA),
                unfocusedContainerColor = Color(0xFF151F2E),
                unfocusedLabelColor = Color(0xFF9EAEBD),
                focusedTextColor = Color(0xFFC8C9E4),
                focusedContainerColor = Color(0xFF151F2E)
            )
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 5.dp, 0.dp, 10.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            OutlinedButton(
                colors = ButtonColors(
                    containerColor = Color(0xFF151F2E),
                    contentColor = Color(0xFFC8C9E4),
                    disabledContainerColor = Color.Red,
                    disabledContentColor = Color.Red
                ),
                modifier = Modifier
                    .border(2.dp, Color(0xFFC8C9E4), RoundedCornerShape(16.dp))
                    .height(35.dp)
                    .width(110.dp),
                enabled = false,
                onClick = { /* if(entry.notes != notes) viewerViewModel.updateMediaNotes(entry.mediaId, notes)*/ },
            ) {
                Text(
                    "Save",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = interFont
                )
            }
            OutlinedButton(
                colors = ButtonColors(
                    containerColor = Color(0xFF151F2E),
                    contentColor = Color(0xFFC8C9E4),
                    disabledContainerColor = Color.Yellow,
                    disabledContentColor = Color.Green,
                ),
                modifier = Modifier
                    .border(2.dp, Color(0xFFC8C9E4), RoundedCornerShape(16.dp))
                    .height(35.dp)
                    .width(110.dp),
                onClick = { /*viewerViewModel.setCurEditingMediaIndex(-1)*/ },
            ) {
                Text(
                    "Return",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontFamily = interFont
                )
            }
        }
    }
}
