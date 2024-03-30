package com.tsundoku.ui.collection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tsundoku.APP_NAME
import com.tsundoku.R
import com.tsundoku.anilist.collection.CollectionViewModel
import com.tsundoku.anilist.viewer.ViewerViewModel
import com.tsundoku.data.TsundokuFilter
import com.tsundoku.interFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CollectionTopAppBar(
    viewerViewModel: ViewerViewModel,
    collectionViewModel: CollectionViewModel
) {
    val searchingState by collectionViewModel.searchingState
    val filteringState by collectionViewModel.filteringState
    if (searchingState) CollectionSearchBar(collectionViewModel)
    else if (filteringState) FilterCollectionWidget(collectionViewModel)
    else CollectionDefaultTopAppBar(viewerViewModel, collectionViewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDefaultTopAppBar(
    viewerViewModel: ViewerViewModel,
    collectionViewModel: CollectionViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF13171D),
            titleContentColor = Color(0xFF9EAEBD),
        ),
        title = {
            Text(
                APP_NAME,
                fontWeight = FontWeight.Bold,
                fontFamily = interFont
            )
        },
        navigationIcon = {
            IconButton(onClick = { collectionViewModel.toggleSearchingState() }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    tint = Color(0xFF42B1EA),
                    contentDescription = "Search Collection Button"

                )
            }
        },
        actions = {
            IconButton(onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    collectionViewModel.setIsRefreshing(true)
                    delay(3000L)
                    fetchTsundokuCollection(viewerViewModel, collectionViewModel)
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    tint = Color(0xFF42B1EA),
                    contentDescription = "Refresh Collection Button"
                )
            }
            IconButton(onClick = { collectionViewModel.toggleFilteringState() }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_filter_list_alt_24),
                    tint = Color(0xFF42B1EA),
                    contentDescription = "Filter Pane Nav Button"
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionSearchBar(
    collectionViewModel: CollectionViewModel
) {
    val searchText by collectionViewModel.searchText.collectAsState()
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF13171D),
            titleContentColor = Color.Yellow,
        ),
        title = { },
        actions = {
            TextField(
                value = searchText,
                onValueChange = collectionViewModel::updateSearchText,
                singleLine = true,
                placeholder = { Text("Search...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        tint = Color(0xFF42B1EA),
                        contentDescription = "Search Collection Icon"
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {
                        collectionViewModel.toggleSearchingState()
                        collectionViewModel.updateSearchText("")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            tint = Color(0xFF42B1EA),
                            contentDescription = "Close Collection Search App Bar"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(25.dp, 0.dp),
                textStyle = TextStyle(
                    fontSize = 15.sp,
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterCollectionWidget(
    collectionViewModel: CollectionViewModel
) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF13171D),
            titleContentColor = Color.Yellow,
        ),
        title = { },
        actions = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(25.dp, 0.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LargeDropdownMenu(
                    collectionViewModel,
                    items = TsundokuFilter.entries.map { it.value } ,
                    selectedIndex = selectedIndex,
                    onItemSelected = { index, curFilter ->
                        selectedIndex = index
                        collectionViewModel.updateFilter(TsundokuFilter.parse(curFilter))
                    },
                )
            }
        },
    )
}

@Composable
fun <T> LargeDropdownMenu(
    collectionViewModel: CollectionViewModel,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    notSetLabel: String? = null,
    items: List<T>,
    selectedIndex: Int,
    onItemSelected: (index: Int, item: T) -> Unit,
    drawItem: @Composable (T, Boolean, Boolean, () -> Unit) -> Unit = { item, selected, itemEnabled, onClick ->
        LargeDropdownMenuItem(
            text = item.toString(),
            selected = selected,
            enabled = itemEnabled,
            onClick = onClick,
        )
    },
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier.height(IntrinsicSize.Min)
    ) {
        OutlinedTextField(
            label = { },
            value = items[selectedIndex].toString(),
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    tint = Color(0xFF42B1EA),
                    contentDescription = "Open Filter List"
                )
            },
            trailingIcon = {
                IconButton(onClick = {
                    if (collectionViewModel.filter.value != TsundokuFilter.NONE) collectionViewModel.updateFilter(TsundokuFilter.NONE)
                    collectionViewModel.toggleFilteringState()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        tint = Color(0xFF42B1EA),
                        contentDescription = "Close Filter App Bar",
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            onValueChange = { },
            readOnly = true,
            textStyle = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = interFont,
                color = Color(0xFF9EAEBD)
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

        // Transparent clickable surface on top of OutlinedTextField
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(top = 8.dp, end = 25.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .clickable(enabled = enabled) { expanded = true },
            color = Color.Transparent,
        ) {}
    }

    if (expanded) {
        Dialog(
            onDismissRequest = { expanded = false },
        ) {
            Surface(
                color = Color(0xFF13171D),
                shape = RoundedCornerShape(12.dp),
            ) {
                val listState = rememberLazyListState()
                if (selectedIndex > -1) {
                    LaunchedEffect("ScrollToSelected") {
                        listState.scrollToItem(index = selectedIndex)
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxWidth(), state = listState) {
                    if (notSetLabel != null) {
                        item {
                            LargeDropdownMenuItem(
                                text = notSetLabel,
                                selected = false,
                                enabled = false,
                                onClick = { },
                            )
                        }
                    }
                    itemsIndexed(items) { index, item ->
                        val selectedItem = index == selectedIndex
                        drawItem(
                            item,
                            selectedItem,
                            true
                        ) {
                            onItemSelected(index, item)
                            expanded = false
                        }

                        if (index < items.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LargeDropdownMenuItem(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = when {
        !enabled -> Color.Yellow
        selected -> Color(0xFF42B1EA)
        else -> Color(0xFF9EAEBD)
    }

    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(modifier = Modifier
            .clickable(enabled) { onClick() }
            .fillMaxWidth()
            .padding(16.dp)) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}