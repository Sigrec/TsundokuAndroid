package com.tsundoku.models

import android.icu.util.Currency
import android.util.Log
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.BigDecimal

class MediaModel {
    companion object {
        val costRegex = Regex("^\\d+[.]?(?:[\\d]{1,2})?\$")
        val volumeNumRegex = Regex()

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
data class Media(
    val viewerId: Int? = null,
    val mediaId: String,
    var curVolumes: Int,
    var maxVolumes: Int,
    var cost: Double
    // @Serializable(with = BigDecimalSerializer::class) var cost: BigDecimal
)

object BigDecimalSerializer: KSerializer<BigDecimal> {
    override fun deserialize(decoder: Decoder): BigDecimal {
        Log.d("TEST", "$decoder | ${decoder.decodeString()}")
        return decoder.decodeString().toBigDecimal()
    }

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toPlainString())
    }

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)
}