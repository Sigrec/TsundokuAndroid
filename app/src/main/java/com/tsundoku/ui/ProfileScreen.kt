package com.tsundoku.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination

@Destination(route = "profile")
@Composable
fun ProfileScreen(
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF13171D))
    ) {
        Text(
            "IN PROFILE SCREEN",
            fontWeight = FontWeight.Bold,
            fontSize = 40.sp,
            color = Color(0xFF9EAEBD)
        )
    }
}