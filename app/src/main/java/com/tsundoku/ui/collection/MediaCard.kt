package com.tsundoku.ui.collection

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tsundoku.ANILIST_MANGA_URL
import com.tsundoku.APP_NAME
import com.tsundoku.MANGADEX_MANGA_URL
import com.tsundoku.anilist.collection.CollectionViewModel
import com.tsundoku.anilist.viewer.ViewerViewModel
import com.tsundoku.data.NetworkResource
import com.tsundoku.interFont
import com.tsundoku.models.CollectionUiState
import com.tsundoku.models.TsundokuItem
import com.tsundoku.models.ViewerModel
import com.tsundoku.models.Website
import kotlinx.coroutines.time.delay
import java.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeMediaCardContainer(
    item: TsundokuItem,
    animationDuration: Int = 500,
    mediaCard: @Composable () -> Unit,
    viewerViewModel: ViewerViewModel,
    collectionViewModel: CollectionViewModel
) {
    var isRemoved by remember { mutableStateOf(false) }
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange =  {
            if (it == SwipeToDismissBoxValue.Settled) {
                isRemoved = true
                true
            } else false
        }
    )

    LaunchedEffect(key1 = isRemoved) {
        if (isRemoved) {
            delay(Duration.ofMillis(animationDuration.toLong()))
            viewerViewModel.deleteDatabaseMedia(listOf(item.mediaId))
            if(item.website == Website.ANILIST) {
                viewerViewModel.getMediaCustomLists(item.mediaId.toInt()).collect {
                    when (it) {
                        is NetworkResource.Success -> {
                            val list = ViewerModel.parseTrueCustomLists(StringBuilder(it.data.customLists.toString().trim()))
                            if (list.contains(APP_NAME)) {
                                viewerViewModel.deleteAniListMediaFromCollection(item.mediaId.toInt(), list)
                            }
                            collectionViewModel.deleteItemFromTsundokuCollection(item)
                            viewerViewModel.decreaseChapterCount(item.chapters)
                            viewerViewModel.decrementSeriesCount()
                            viewerViewModel.decreaseVolumesCount(item.curVolumes.value.toInt())
                            viewerViewModel.decreaseCollectionCost(item.cost)
                            viewerViewModel.decrementStatusCount(item)
                        }
                        is NetworkResource.Loading -> { Log.d("Tsundoku", "Loading Custom Lists for Media ${item.mediaId}") }
                        else -> Log.e("Tsundoku", "Getting Custom Lists for Media ${item.mediaId} Failed")
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = !isRemoved,
        exit = shrinkVertically(
            animationSpec = tween(animationDuration),
            shrinkTowards = Alignment.Top
        ) + fadeOut(),
        modifier = Modifier.border(0.dp, Color.Transparent, RoundedCornerShape(8.dp))
    ) {
        SwipeToDismissBox(
            state = state,
            backgroundContent = { DeleteTsundokuItemBackground(swipeDismissState = state) },
            modifier = Modifier.clip(RoundedCornerShape(8.dp)),
            content =  { mediaCard() },
            enableDismissFromStartToEnd = false,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteTsundokuItemBackground(
    swipeDismissState: SwipeToDismissBoxState
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (swipeDismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) Color(
                    0x339EAEBD
                ) else Color.Transparent
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Delete",
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = interFont,
            color = Color(0xFF42B1EA)
        )
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete Tsundoku Item",
            tint = Color(0xFF42B1EA),
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
fun MediaCard(
    item: TsundokuItem,
    viewerViewModel: ViewerViewModel,
    collectionViewModel: CollectionViewModel,
    collectionUiState: CollectionUiState,
    uriHandler: UriHandler
) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F232D)),
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth(.90f)
            .border(2.dp, Color(0xFF3E485C), RoundedCornerShape(8.dp))
    ) {
        Row {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth(0.22f)
                    .clickable {
                        if (collectionUiState.curEditingMediaIndex == -1) {
                            when (item.website) {
                                Website.ANILIST -> uriHandler.openUri("$ANILIST_MANGA_URL/${item.mediaId}")
                                Website.MANGADEX -> uriHandler.openUri("$MANGADEX_MANGA_URL/${item.mediaId}")
                            }
                        }
                    },
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                clipToBounds = true,
                filterQuality = FilterQuality.High
            )
            Column (
                modifier = Modifier
                    .padding(7.dp, 4.dp, 10.dp, 10.dp)
                    .weight(1f)
            ) {
                Text(
                    text = "${item.format} | ${item.status}",
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF42B1EA),
                    fontFamily = interFont
                )
                Text(
                    text = item.title,
                    modifier = Modifier
                        //.offset(y = (-4).dp)
                        .clickable {
                            if (collectionUiState.onViewer && collectionUiState.curEditingMediaIndex == -1) {
                                viewerViewModel.turnOffTopAppBar()
                                viewerViewModel.setSelectedItemIndex(collectionViewModel.getTsundokuItemIndex(item))
                            }
                         },
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color(0xFF9EAEBD),
                    overflow = TextOverflow.Ellipsis,
                    softWrap = true,
                    lineHeight = 18.sp,
                    fontFamily = interFont,
                    maxLines = 2
                )
                Row (
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedButton(
                        colors = ButtonColors(
                            containerColor = Color(0xFF2B2D42),
                            contentColor = Color(0xFF42B1EA),
                            disabledContainerColor = Color(0xFF42B1EA),
                            disabledContentColor = Color(0xFF2B2D42)
                        ),
                        enabled = item.curVolumes.value.isNotBlank() && item.curVolumes.value != "0",
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(2.dp, Color(0xFF3E485C)),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .height(35.dp)
                            .width(35.dp),
                        onClick = {
                            if (collectionUiState.onViewer && item.curVolumes.value.isNotBlank() && item.curVolumes.value != "0") {
                                item.curVolumes.value = (item.curVolumes.value.toInt() - 1).toString()
                                viewerViewModel.addUpdatedCollectionItem(item.mediaId)
                                viewerViewModel.decreaseVolumesCount(1)
                            }
                        },
                    ) {
                        Text(
                            text = "-",
                            modifier =  Modifier.offset(y = (-15).dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 50.sp,
                            fontFamily = interFont
                        )
                    }
                    Box (
                        modifier = Modifier
                            .height(35.dp)
                            .weight(1f)
                            .border(2.dp, Color(0xFF3E485C), RoundedCornerShape(6.dp))
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2B2D42)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .fillMaxHeight(0.65f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x339EAEBD)),
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF42B1EA))
                                    .fillMaxHeight()
                                    .fillMaxWidth(if (item.maxVolumes.value.isNotBlank() && item.curVolumes.value.isNotBlank() && item.maxVolumes.value.toInt() != 0) (item.curVolumes.value.toFloat() / item.maxVolumes.value.toFloat()) else 0f)
                            )
                            Text(
                                text = "${item.curVolumes.value}/${item.maxVolumes.value}",
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .offset(y = (-1).dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color(0xFF9EAEBD),
                                fontFamily = interFont,
                            )
                        }
                    }
                    OutlinedButton(
                        colors = ButtonColors(
                            containerColor = Color(0xFF2B2D42),
                            contentColor = Color(0xFF42B1EA),
                            disabledContainerColor = Color(0xFF42B1EA),
                            disabledContentColor = Color(0xFF2B2D42)
                        ),
                        enabled = item.maxVolumes.value.isNotBlank() && item.curVolumes.value.isNotBlank() && item.curVolumes.value.toInt() != item.maxVolumes.value.toInt(),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(2.dp, Color(0xFF3E485C)),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .height(35.dp)
                            .width(35.dp),
                        onClick = {
                            if (collectionUiState.onViewer && item.curVolumes.value != item.maxVolumes.value) {
                                item.curVolumes.value = (item.curVolumes.value.toInt() + 1).toString()
                                viewerViewModel.addUpdatedCollectionItem(item.mediaId)
                                viewerViewModel.increaseVolumeCount(1)
                            }
                        },
                    ) {
                        Text(
                            text = "+",
                            modifier =  Modifier.offset(x = (-0.5).dp, y = (-6.5).dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 35.sp,
                            fontFamily = interFont
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun MediaCardBottomPreview() {
    Row (
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        OutlinedButton(
            colors = ButtonColors(
                containerColor = Color(0xFF2B2D42),
                contentColor = Color(0xFF42B1EA),
                disabledContainerColor = Color.Yellow,
                disabledContentColor = Color.Green
            ),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(2.dp, Color(0xFF3E485C)),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .height(35.dp)
                .width(35.dp),
            onClick = { /* userViewModel.setCurEditingMediaIndex(-1) */ },
        ) {
            Text(
                text = "+",
                modifier =  Modifier.offset(x = (-0.5).dp, y = (-3).dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
                color = Color(0xFF42B1EA),
                fontFamily = interFont
            )
        }
        Box (
            modifier = Modifier
                .height(35.dp)
                .weight(1f)
                .border(2.dp, Color(0xFF3E485C), RoundedCornerShape(6.dp))
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF2B2D42)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.65f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x339EAEBD)),
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF42B1EA))
                        .fillMaxHeight()
                        .fillMaxWidth(0.4f)// (curVolumes / maxVolumes).toFloat()
                        .clip(RoundedCornerShape(8.dp))
                )
                Text(
                    text = "888/888",
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF9EAEBD),
                    fontFamily = interFont,
                )
            }
        }
        OutlinedButton(
            colors = ButtonColors(
                containerColor = Color(0xFF2B2D42),
                contentColor = Color(0xFF42B1EA),
                disabledContainerColor = Color.Yellow,
                disabledContentColor = Color.Green
            ),
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(2.dp, Color(0xFF3E485C)),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .height(35.dp)
                .width(35.dp),
            onClick = { /* userViewModel.setCurEditingMediaIndex(-1) */ },
        ) {
            Text(
                text = "-",
                modifier =  Modifier.offset(y = (-13).dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 50.sp,
                color = Color(0xFF42B1EA),
                fontFamily = interFont
            )
        }
    }
}