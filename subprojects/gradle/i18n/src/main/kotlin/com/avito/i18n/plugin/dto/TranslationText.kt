package com.avito.i18n.plugin.dto

import com.avito.i18n.plugin.service.TranslationTextSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = TranslationTextSerializer::class)
internal sealed class TranslationText {

    @Serializable
    data class Text(
        @SerialName("text") val text: String,
    ) : TranslationText()

    @Serializable
    data class Plural(
        @SerialName("plural") val plural: TranslationPluralText
    ) : TranslationText()
}

@Serializable
internal data class TranslationPluralText(
    @SerialName("zero") val zero: String?,
    @SerialName("one") val one: String?,
    @SerialName("two") val two: String?,
    @SerialName("few") val few: String?,
    @SerialName("many") val many: String?,
    @SerialName("other") val other: String?,
) {
    fun toMap(): Map<String, String> =
        buildMap {
            zero?.let { this["zero"] = it }
            one?.let { this["one"] = it }
            two?.let { this["two"] = it }
            few?.let { this["few"] = it }
            many?.let { this["many"] = it }
            other?.let { this["other"] = it }
        }

    companion object {
        fun fromMap(map: Map<String, String>): TranslationPluralText =
            TranslationPluralText(
                zero = map["zero"],
                one = map["one"],
                two = map["two"],
                few = map["few"],
                many = map["many"],
                other = map["other"],
            )
    }
}
