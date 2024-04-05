package com.tsundoku

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp

// Tsundoku App
const val APP_NAME = "Tsundoku"
const val TSUNDOKU_SCHEME = "tsundoku"
const val TSUNDOKU_DEFAULT_CURRENCY_CODE = "USD"
const val TSUNDOKU_DEFAULT_CURRENCY_SYMBOL = "$"
val TSUNDOKU_COLLECTION_CARD_GAP = 15.dp
val TSUNDOKU_BUTTON_CORNER_ROUNDING = 8.dp

// AniList
const val ANILIST_URL = "https://anilist.co"
const val ANILIST_CLIENT_ID = 17514
const val ANILIST_MANGA_URL = "$ANILIST_URL/manga"
const val ANILIST_AUTH_URL = "$ANILIST_URL/api/v2/oauth/authorize?client_id=$ANILIST_CLIENT_ID&response_type=token"
const val ANILIST_GRAPHQL_BASE_URL = "https://graphql.anilist.co"

// Mangadex
const val MANGADEX_URL = "https://mangadex.org"
const val MANGADEX_MANGA_URL = "$MANGADEX_URL/title"

// Supabase DB
const val DATABASE_VIEWER_TABLE = "viewer"
const val DATABASE_MEDIA_TABLE = "media"
const val SUPABASE_URL = "https://bmxyucfasyjxgjavjcdi.supabase.co"
const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJteHl1Y2Zhc3lqeGdqYXZqY2RpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTA5NTIzNzksImV4cCI6MjAyNjUyODM3OX0.ySH49W8MmUFwE-z9CbELCalcufvHGWJQgjVSsHWLllc"

// Fonts
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val interFont = FontFamily(Font(googleFont = GoogleFont("Inter"), weight = FontWeight.Bold, fontProvider = provider))

// const val ANILIST_CALLBACK_URL = "$TSUNDOKU_SCHEME://app"
// const val ANILIST_AUTH_DEEPLINK = "$ANILIST_CALLBACK_URL#access_token={accessToken}&token_type=Bearer&expires_in=31536000"