package com.tsundoku.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination

@Destination(route = "addmedia")
@Composable
fun AddMediaScreen() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
        // .background(TsundokuBackground)
    ) {
        Text(
            "IN ADD MEDIA SCREEN",
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp,
            // color = AniListGrey
        )
    }
}