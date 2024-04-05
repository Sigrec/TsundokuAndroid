package com.tsundoku.models

import android.icu.util.Currency
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.tsundoku.GetMediaEntryQuery
import com.tsundoku.data.TsundokuFormat
import com.tsundoku.data.TsundokuStatus
import com.tsundoku.fragment.MediaListEntry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonUnquotedLiteral
import kotlinx.serialization.json.jsonPrimitive
import java.math.BigDecimal

class MediaModel {
    companion object {
        val costRegex = Regex("^\\d+[.]?(?:[\\d]{1,2})?\$")
        val volumeNumRegex = Regex("^\\d{0,3}")
        val validateUUIDRegex = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\$")

        fun parseAniListMedia(media: MediaListEntry.Media, dbMedia: Media): TsundokuItem {
            return TsundokuItem(
                mediaId = media.id.toString(),
                website = Website.ANILIST,
                title = media.title!!.userPreferred!!,
                countryOfOrigin = media.countryOfOrigin.toString(),
                status = getMediaStatus(media.status!!.name),
                format = getCorrectFormat(
                    media.format!!.name,
                    media.countryOfOrigin.toString()
                ),
                chapters = media.chapters ?: 0,
                notes = media.mediaListEntry?.notes ?: "",
                imageUrl = media.coverImage!!.medium!!,
                curVolumes = mutableStateOf(dbMedia.curVolumes.toString()),
                maxVolumes = mutableStateOf(dbMedia.maxVolumes.toString()),
                cost = dbMedia.cost
            )
        }

        fun parseAniListMedia(media: GetMediaEntryQuery.Media, curVolumes: Int, maxVolumes: Int, cost: BigDecimal): TsundokuItem {
            return TsundokuItem(
                mediaId = media.id.toString(),
                website = Website.ANILIST,
                title = media.title!!.userPreferred!!,
                countryOfOrigin = media.countryOfOrigin.toString(),
                status = getMediaStatus(media.status!!.name),
                format = getCorrectFormat(
                    media.format!!.name,
                    media.countryOfOrigin.toString()
                ),
                chapters = media.chapters ?: 0,
                notes = if(media.mediaListEntry != null) media.mediaListEntry.notes ?: "" else "",
                imageUrl = media.coverImage!!.medium!!,
                curVolumes = mutableStateOf(curVolumes.toString()),
                maxVolumes = mutableStateOf(maxVolumes.toString()),
                cost = cost
            )
        }

        /**
         * Gets the correct format for a series
         * @param format The input format form AniList or Mangadex to be parsed
         * @param countryOfOrigin The country where this series was made
         */
        fun getCorrectFormat(format: String, countryOfOrigin: String): TsundokuFormat {
            return if (format == "MANGA") {
                when (countryOfOrigin) {
                    "JP", "jp" -> TsundokuFormat.MANGA
                    "KR", "ko" -> TsundokuFormat.MANHWA
                    "CN", "TW", "zh", "zh-hk" -> TsundokuFormat.MANHUA
                    "FR", "fr" -> TsundokuFormat.MANFRA
                    "EN", "en" -> TsundokuFormat.COMIC
                    else -> TsundokuFormat.ERROR
                }
            } else TsundokuFormat.NOVEL
        }

        /**
         * Gets the status of a series converted into
         */
        fun getMediaStatus(status: String): TsundokuStatus {
            return when (status) {
                "FINISHED" -> TsundokuStatus.FINISHED
                "RELEASING" -> TsundokuStatus.ONGOING
                "NOT_YET_RELEASED" -> TsundokuStatus.COMING_SOON
                "CANCELLED" -> TsundokuStatus.CANCELLED
                "HIATUS" -> TsundokuStatus.HIATUS
                else -> TsundokuStatus.ERROR
            }
        }

        /**
         * Get the currency symbol based on its currency code
         * @param currencyCode The code used to get the symbol
         */
        fun getCurrencySymbol(currencyCode: String): String = Currency.getInstance(currencyCode).symbol

        /**
         * Get the currency code based on its currency symbol
         * @param currencySymbol THe symbol used to get the code
         */
        fun getCurrencyCode(currencySymbol: String): String {
            Log.d("TEST", "Currency Symbol = $currencySymbol")
            val currency = Currency.getAvailableCurrencies().find { it.symbol == currencySymbol }
            return if (currency != null) currency.currencyCode
            else "USD"
        }
    }
}

@Serializable
data class VolumeUpdateMedia(
    val mediaId: String,
    val viewerId: Int,
    var curVolumes: String,
)

@Serializable
data class Media(
    val viewerId: Int? = null,
    val mediaId: String,
    var curVolumes: Int,
    var maxVolumes: Int,
    @Serializable(with = BigDecimalSerializer::class) var cost: BigDecimal,
    var notes: String? = null
)


object BigDecimalSerializer: KSerializer<BigDecimal> {
    override fun deserialize(decoder: Decoder): BigDecimal {
        return when (decoder) {
            is JsonDecoder -> decoder.decodeJsonElement().jsonPrimitive.content.toBigDecimal()
            else -> decoder.decodeString().toBigDecimal()
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: BigDecimal) {
        when (encoder) {
            // use JsonUnquotedLiteral() to encode the BigDecimal literally
            is JsonEncoder -> encoder.encodeJsonElement(JsonUnquotedLiteral(value.toPlainString()))
            else -> encoder.encodeString(value.toPlainString())
        }
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)
}