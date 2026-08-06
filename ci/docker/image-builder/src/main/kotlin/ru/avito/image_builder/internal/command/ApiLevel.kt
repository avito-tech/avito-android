package ru.avito.image_builder.internal.command

internal sealed interface ApiLevel {

    val sdkInt: Int

    data class Major(override val sdkInt: Int) : ApiLevel {
        override fun toString(): String = "$sdkInt"
    }

    data class MajorMinor(override val sdkInt: Int, val minor: Int) : ApiLevel {
        override fun toString(): String = "$sdkInt.$minor"
    }

    companion object {

        fun parse(value: String): ApiLevel {
            val parts = value.split('.')
            val numbers = parts.mapNotNull(String::toIntOrNull)
            require(numbers.size == parts.size && numbers.size in 1..2) {
                "Invalid api level '$value'. Expected 30, 36 or 37.0"
            }
            return if (numbers.size == 1) {
                Major(numbers.first())
            } else {
                MajorMinor(numbers.first(), numbers.last())
            }
        }
    }
}
