package com.tsundoku.models

import android.icu.util.Currency
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

        fun getCorrectFormat(format: String, countryOfOrigin: String): String {
            return if (format == "MANGA") {
                when (countryOfOrigin) {
                    "KR", "ko" -> "Manhwa"
                    "CN", "TW", "zh", "zh-hk" -> "Manhua"
                    "FR", "fr" -> "Manfra"
                    "EN", "en" -> "Comic"
                    else -> "Manga"
                }
            } else "Novel"
        }

        /**
         * Get the currency symbol based on its currency code
         * @param currencyCode THe code to used to get the symbol
         */
        fun getCurrencySymbol(currencyCode: String): String = Currency.getInstance(currencyCode).symbol
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