package com.tsundoku.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tsundoku.ANILIST_AUTH_URL
import com.tsundoku.R

fun Context.openActionView(uri: Uri) {
    try {
        Intent(Intent.ACTION_VIEW, uri).apply {
            startActivity(this)
        }
    } catch (e: ActivityNotFoundException) {
        // showToast(getString(R.string.no_app_found_for_this_action))
        Log.e("ANILIST", "No App Found for this Action")
    }
}

@Composable
fun LoginScreen() {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF13171D),
    ){
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
//        Icon(
//            Icons.Filled.AccountCircle,
//            contentDescription = stringResource(id = R.string.login_icon_desc),
//            modifier = Modifier.size(height = 250.dp, width = 250.dp),
//            tint = Color(0xFF9EAEBD)
//        )
            Text(
                stringResource(id = R.string.login_title),
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = Color(0xFF9EAEBD)
            )
            ElevatedButton(
                onClick = { context.openActionView(Uri.parse(ANILIST_AUTH_URL)) },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .width(20.dp),
                 border = BorderStroke(width = 2.dp, color = Color(0xFF42B1EA)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF191D26),
                contentColor = Color(0xFF42B1EA)
            ),
                shape = RoundedCornerShape(8),
                elevation =  ButtonDefaults.buttonElevation(
                    defaultElevation = 10.dp,
                    pressedElevation = 15.dp,
                    disabledElevation = 0.dp,

                    ),
                content = {
                    Text(
                        stringResource(id = R.string.login_button_text),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp)
                }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    stringResource(id = R.string.anilist),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF9EAEBD)
                )
                ClickableText(
                    text = AnnotatedString(stringResource(id = R.string.guidelines)),
                    style = TextStyle(
                        textDecoration = TextDecoration.Underline,
                        color = Color(0xFF42B1EA),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    onClick = { uriHandler.openUri("https://anilist.co/forum/thread/14") }
                )
            }
        }
    }
}