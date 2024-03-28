package com.tsundoku.ui.loading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination
import com.tsundoku.interFont

@Composable
fun LoadingScreen() {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF13171D)),
    ) {
        CircularProgressIndicator(
            modifier = Modifier.fillMaxSize(0.65f),
            color = Color(0xFF42B1EA),
            trackColor = Color(0xFF9EAEBD),
        )
        Text (
            text = "Loading Tsundoku Collection",
            color = Color(0xFF9EAEBD),
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = interFont,
        )
    }
}

@Preview
@Composable
fun LoadingScreenPreview() {
    LoadingScreen()
}