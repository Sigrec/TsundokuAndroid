package com.tsundoku

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont

const val APP_NAME = "Tsundoku"
const val TSUNDOKU_SCHEME = "tsundoku"

const val ANILIST_URL = "https://anilist.co"
const val ANILIST_CLIENT_ID = 17514
const val ANILIST_MANGA_URL = "$ANILIST_URL/manga/"
const val ANILIST_AUTH_URL = "$ANILIST_URL/api/v2/oauth/authorize?client_id=$ANILIST_CLIENT_ID&response_type=token"
const val ANILIST_GRAPHQL_BASE_URL = "https://graphql.anilist.co"

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val interFont = FontFamily(Font(googleFont = GoogleFont("Inter"), weight = FontWeight.Bold, fontProvider = provider))

// const val ANILIST_CALLBACK_URL = "$TSUNDOKU_SCHEME://app"
// const val ANILIST_AUTH_DEEPLINK = "$ANILIST_CALLBACK_URL#access_token={accessToken}&token_type=Bearer&expires_in=31536000"