package com.tsundoku.ui.loading


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(50.dp).align(Alignment.TopCenter),
            color = Color(0xFF42B1EA),
            trackColor = Color(0xFF9EAEBD),
        )
    }
}

@Preview
@Composable
fun LoadingIndicatorPreview() {
    LoadingIndicator()
}