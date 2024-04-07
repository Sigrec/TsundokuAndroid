package com.tsundoku.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hd.charts.PieChartView
import com.hd.charts.common.model.ChartDataSet
import com.hd.charts.style.ChartViewDefaults
import com.hd.charts.style.PieChartDefaults
import com.ramcosta.composedestinations.annotation.Destination
import com.tsundoku.anilist.viewer.ViewerViewModel
import com.tsundoku.interFont
import com.tsundoku.models.MediaModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Currency

@Destination(route = "profile")
@Composable
fun ProfileScreen(
    viewerViewModel: ViewerViewModel,
) {
    LaunchedEffect(Unit) {
        viewerViewModel.turnOffTopAppBar()
    }
    val viewerState by viewerViewModel.viewerState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // TODO - Add pie chart and functionality
    // TODO - Add logout functionality
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .zIndex(3f)
                .offset(y = (-180).dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth(0.33f)
                    .fillMaxHeight(0.16f)
                    .clip(RoundedCornerShape(21.dp))
                    .border(2.dp, Color(0xFF42B1EA), RoundedCornerShape(21.dp)),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(viewerState.viewer?.avatar?.medium)
                    .crossfade(true)
                    .build(),
                contentDescription = "Viewer AniList Avatar Image",
                contentScale = ContentScale.FillBounds,
                clipToBounds = true,
                filterQuality = FilterQuality.High
            )
            Text(
                text = viewerState.viewer!!.name,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = interFont,
                fontSize = 25.sp,
                color = Color(0xFF777A9E)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF13171D)),
            verticalArrangement = Arrangement.spacedBy((-30).dp),
        ) {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.3f),
                model = ImageRequest.Builder(LocalContext.current)
                    .data(viewerState.viewer?.bannerImage)
                    .crossfade(true)
                    .build(),
                contentDescription = "Viewer AniList Banner Image",
                contentScale = ContentScale.FillBounds,
                clipToBounds = true,
                filterQuality = FilterQuality.High
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .zIndex(2f)
                    .clip(RoundedCornerShape(30.dp, 30.dp, 0.dp, 0.dp))
                    .background(Color(0xFF13171D)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF13171D))
                        .offset(y = 130.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(13.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileStat(title = "Chapters", value = viewerState.chapters.toString())
                        ProfileStat(title = "Series Count", value = viewerState.seriesCount.toString())
                        ProfileStat(title = "Volumes", value = viewerState.volumes.toString())
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileStat(title = "Value", value = "${viewerState.currencySymbol}${viewerState.collectionCost}")
                    }
                    ProfileSeparator()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OverviewPieChart(
                            title = "Status Overview",
                            pieColors = listOf(Color(0xFF42B1EA), Color(0xFF6CCDFF), Color(0xFF84D5FF), Color(0xFFB2E5FF), Color(0xFF9BD4F1)),
                            pieSeparatorColor = Color(0xFF2B2D42),
                            data = viewerViewModel.getStatusData(),
                        )
                    }
                    val statusData = viewerViewModel.getStatusData()
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChartLegend("Finished", statusData [0], Color(0xFF42B1EA))
                            ChartLegend("Ongoing", statusData [1], Color(0xFF6CCDFF))
                            ChartLegend("Cancelled", statusData [2], Color(0xFF84D5FF))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ChartLegend("Hiatus", statusData [3], Color(0xFFB2E5FF))
                            ChartLegend("Coming Soon", statusData [4], Color(0xFF9BD4F1))
                        }
                    }
                    ProfileSeparator()

                    val currencySymbols = Currency.getAvailableCurrencies().map { it.symbol }.sortedBy { it }.sortedBy { it.length }
                    var selectedIndex by remember { mutableIntStateOf(currencySymbols.indexOf(viewerState.currencySymbol)) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DialogDropdownMenu(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(130.dp),
                            items = currencySymbols,
                            enableTrailingIcon = false,
                            enableLeadingIcon = false,
                            selectedIndex = selectedIndex,
                            onItemSelected = { index, curSymbol ->
                                selectedIndex = index
                                val currencyCode = MediaModel.getCurrencyCode(curSymbol)
                                viewerViewModel.setCurrencyCode(currencyCode)
                                viewerViewModel.setCurrencySymbol(curSymbol)
                                coroutineScope.launch(Dispatchers.IO) {
                                    viewerViewModel.updateCurrencyCode(currencyCode)
                                }
                            },
                        )
                        Button(
                            onClick = {  },
                            border = BorderStroke(2.dp, Color(0xFF42B1EA)),
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(130.dp),
                            // border = BorderStroke(width = 2.dp, color = Color(0xFF42B1EA)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2B2D42),
                                contentColor = Color(0xFF42B1EA)
                            ),
                            content = {
                                Text(
                                    text = "Log Out",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = interFont,
                                    fontSize = 20.sp,
                                    color = Color(0xFF9EAEBD),
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStat(
    value: String,
    title: String,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = interFont,
            fontSize = 18.sp,
            color = Color(0xFF42B1EA)
        )
        Text(
            text = title,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = interFont,
            fontSize = 18.sp,
            color = Color.White
        )
    }
}

@Composable
fun ProfileSeparator(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(0.9f),
        thickness = 2.dp,
        color = Color(0xFF42B1EA)
    )
}

@Composable
private fun OverviewPieChart(
    title: String,
    postFix: String = "",
    pieColors: List<Color>,
    pieSeparatorColor: Color,
    data: List<Float>
) {
    val dataSet = ChartDataSet(
        items = data,
        title = title,
        postfix = postFix,
    )

    val style = PieChartDefaults.style(
        borderColor = pieSeparatorColor,
        donutPercentage = 0f,
        borderWidth = 8f,
        pieColors = pieColors,
        chartViewStyle = ChartViewDefaults.style(
            backgroundColor = Color.Transparent,
            cornerRadius = 0.dp,
            shadow = 0.dp,
            outerPadding = 0.dp,
            innerPadding = 0.dp,
            width = 230.dp,
        )
    )

    PieChartView(
        dataSet = dataSet,
        style = style
    )
}

@Composable
private fun ChartLegend(
    title: String,
    value: Float,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(20.dp).clip(RectangleShape).background(color)
        )
        Text(
            text = "$title (${value.toInt()})",
            fontWeight = FontWeight.ExtraBold,
            fontFamily = interFont,
            fontSize = 15.sp,
            color = Color(0xFF9EAEBD),
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Preview
@Composable
fun ProfilePreview() {
    // pieColors = listOf(Color(0xFF42B1EA), Color(0xFF6CCDFF), Color(0xFF84D5FF), Color(0xFFB2E5FF), Color(0xFF9BD4F1))
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChartLegend("Finished", 3.0f, Color(0xFF42B1EA))
            ChartLegend("Ongoing", 3.0f, Color(0xFF6CCDFF))
            ChartLegend("Cancelled", 3.0f, Color(0xFF84D5FF))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ChartLegend("Hiatus", 3.0f, Color(0xFFB2E5FF))
            ChartLegend("Coming Soon", 3.0f, Color(0xFF9BD4F1))
        }
    }
//    Box(
//        modifier = Modifier
//            .fillMaxSize(),
//        contentAlignment = Alignment.Center,
//    ) {
//        Column(
//            modifier = Modifier
//                .zIndex(3f)
//                .offset(y = (-180).dp),
//            verticalArrangement = Arrangement.spacedBy(10.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ){
//            AsyncImage(
//                modifier = Modifier
//                    .fillMaxWidth(0.33f)
//                    .fillMaxHeight(0.18f)
//                    .clip(RoundedCornerShape(21.dp))
//                    .border(2.dp, Color(0xFF42B1EA), RoundedCornerShape(21.dp)),
//                model = ImageRequest.Builder(LocalContext.current)
//                    .data("")
//                    .crossfade(true)
//                    .build(),
//                contentDescription = "Viewer AniList Avatar Image",
//                contentScale = ContentScale.FillBounds,
//                clipToBounds = true,
//                filterQuality = FilterQuality.High
//            )
//            Text(
//                text = "Preminence",
//                fontWeight = FontWeight.ExtraBold,
//                fontFamily = interFont,
//                fontSize = 25.sp,
//                color = Color(0xFF777A9E)
//            )
//        }
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color(0xFF13171D)),
//            verticalArrangement = Arrangement.spacedBy((-30).dp),
//        ) {
//            AsyncImage(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .fillMaxHeight(0.3f),
//                model = ImageRequest.Builder(LocalContext.current)
//                    .data("")
//                    .crossfade(true)
//                    .build(),
//                contentDescription = "Viewer AniList Banner Image",
//                contentScale = ContentScale.FillBounds,
//                clipToBounds = true,
//                filterQuality = FilterQuality.High
//            )
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                    .zIndex(2f)
//                    .clip(RoundedCornerShape(30.dp, 30.dp, 0.dp, 0.dp))
//                    .background(Color(0xFF13171D)),
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(Color(0xFF13171D))
//                        .offset(y = 130.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    verticalArrangement = Arrangement.spacedBy(13.dp)
//                ) {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceAround,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        ProfileStat(title = "Chapters", value = "1289")
//                        ProfileStat(title = "Series Count", value = "109")
//                        ProfileStat(title = "Volumes", value = "489")
//                    }
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceAround,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        ProfileStat(title = "Value", value = "$23502363247347.00")
//                    }
//                    ProfileSeparator()
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(IntrinsicSize.Min)
//                    ) {
//
//                    }
//                    ProfileSeparator()
//                    val currencySymbols = Currency.getAvailableCurrencies().map { it.symbol }.sortedBy { it }.sortedBy { it.length }
//                    var selectedIndex by remember { mutableIntStateOf(0) }
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(40.dp),
//                        horizontalArrangement = Arrangement.SpaceAround,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        DialogDropdownMenu(
//                            modifier = Modifier
//                                .fillMaxHeight()
//                                .width(125.dp),
//                            items = currencySymbols,
//                            enableTrailingIcon = false,
//                            enableLeadingIcon = false,
//                            selectedIndex = selectedIndex,
//                            onItemSelected = { index, curSymbol ->
//                                selectedIndex = index
//                                val currencyCode = MediaModel.getCurrencyCode(curSymbol)
////                                viewerViewModel.setCurrencyCode(currencyCode)
////                                viewerViewModel.setCurrencySymbol(curSymbol)
////                                coroutineScope.launch(Dispatchers.IO) {
////                                    viewerViewModel.updateCurrencyCode(currencyCode)
////                                }
//                            },
//                            onClosed = { /* Do nothing */ }
//                        )
//                        Button(
//                            onClick = {  },
//                            border = BorderStroke(2.dp, Color(0xFF42B1EA)),
//                            modifier = Modifier
//                                .fillMaxHeight()
//                                .width(125.dp),
//                            // border = BorderStroke(width = 2.dp, color = Color(0xFF42B1EA)),
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = Color(0xFF2B2D42),
//                                contentColor = Color(0xFF42B1EA)
//                            ),
//                            content = {
//                                Text(
//                                    text = "Log Out",
//                                    fontWeight = FontWeight.ExtraBold,
//                                    fontFamily = interFont,
//                                    fontSize = 20.sp,
//                                    color = Color(0xFF9EAEBD)
//                                )
//                            }
//                        )
//                    }
//                }
//            }
//        }
//    }
}