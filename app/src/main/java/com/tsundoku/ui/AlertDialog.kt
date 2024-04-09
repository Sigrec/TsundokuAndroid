package com.tsundoku.ui

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tsundoku.interFont

@Composable
fun AlertDialog(
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(
                icon,
                contentDescription = "Alert Icon",
                modifier = Modifier.size(33.dp)
            )
        },
        title = {
            Text(
                text = "$dialogText $dialogTitle",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .wrapContentSize(Alignment.Center),
                fontFamily = interFont,
                fontWeight = FontWeight.SemiBold,
            )
        },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {  },
        dismissButton = {
            TextButton(
                onClick = { onDismissRequest() },
                colors = ButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF42B1EA),
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = Color.Transparent
                )
            ) {
                Text(
                    "Dismiss",
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        },
        containerColor = Color(0xD9393B51),
        textContentColor = Color(0xFF42B1EA),
        iconContentColor = Color(0xFF42B1EA),
        titleContentColor = Color(0xFF9EAEBD)
    )
}

@Preview
@Composable
fun AlertDialogPreview() {
    AlertDialog(
        dialogTitle = "Does not Exist!",
        dialogText = "\"Naruto\"",
        icon = Icons.Default.Info,
        onDismissRequest = {  },
        onConfirmation = {  },
    )
}