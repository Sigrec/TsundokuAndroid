package com.tsundoku.ui.collection

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.tsundoku.GetTsundokuCollectionQuery
import com.tsundoku.anilist.user.UserViewModel
import com.tsundoku.interFont
import com.tsundoku.ui.model.CollectionUiState
import com.tsundoku.ui.model.MediaModel
import com.tsundoku.ui.theme.TextColorPrimary
import com.tsundoku.ui.theme.TextColorSecondary


@Composable
fun MediaCard(
    entry: GetTsundokuCollectionQuery.Entry,
    index: Int,
    userViewModel: UserViewModel,
    collectionUiState: CollectionUiState,
    uriHandler: UriHandler
) {
    val media = entry.mediaListEntry.media!!
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
                    .clickable { if (collectionUiState.curEditingMediaIndex == -1) uriHandler.openUri("$ANILIST_MANGA_URL${media.id}") },
                model = ImageRequest.Builder(LocalContext.current)
                    .data(media.coverImage!!.medium.toString())
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
                    text = MediaModel.getCorrectFormat(media.format!!.name, media.countryOfOrigin.toString()),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF42B1EA),
                    fontFamily = interFont
                )
                Text(
                    text = media.title!!.userPreferred!!,
                    modifier = Modifier
                        //.offset(y = (-4).dp)
                        .clickable { if (collectionUiState.curEditingMediaIndex == -1) userViewModel.setCurEditingMediaIndex(index) },
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextColorPrimary,
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
                            contentColor = TextColorSecondary,
                            disabledContainerColor = Color.Yellow,
                            disabledContentColor = Color.Green
                        ),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(2.dp, Color(0xFF3E485C)),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .height(35.dp)
                            .width(35.dp),
                        onClick = {  },
                    ) {
                        Text(
                            text = "+",
                            modifier =  Modifier.offset(x = (-0.5).dp, y = (-6.5).dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 35.sp,
                            color = TextColorSecondary,
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
                                    .fillMaxWidth(0.4f) // TODO - Set to (curVolumes / maxVolumes).toFloat()
                            )
                        }
                    }
                    OutlinedButton(
                        colors = ButtonColors(
                            containerColor = Color(0xFF2B2D42),
                            contentColor = TextColorSecondary,
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
                            modifier =  Modifier.offset(y = (-15).dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 50.sp,
                            color = TextColorSecondary,
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
                contentColor = TextColorSecondary,
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
                color = TextColorSecondary,
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
            }
        }
        OutlinedButton(
            colors = ButtonColors(
                containerColor = Color(0xFF2B2D42),
                contentColor = TextColorSecondary,
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
                color = TextColorSecondary,
                fontFamily = interFont
            )
        }
    }
}