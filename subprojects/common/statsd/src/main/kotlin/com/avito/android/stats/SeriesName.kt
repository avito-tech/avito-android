package com.avito.android.stats

import java.io.Serializable

/**
 * https://graphite.readthedocs.io/en/latest/terminology.html#term-series
 *
 * part with dot separator will be treated as single series name part
 *
 * example:
 *  - SeriesName.create("one.two.three") -> "one_two_three"
 *  - SeriesName.create("one", "two.three") -> "one.two_three"
 * use:
 *  - SeriesName.create("one", "two", "three") -> "one.two.three"
 *  - SeriesName.create("one.two.three", multipart = true) -> "one.two.three"
 *
 *  same rules applies for append
 */
public class SeriesName private constructor(
    private val parts: List<String>
) : Serializable {

    public fun prefix(seriesName: SeriesName): SeriesName {
        return SeriesName(seriesName.parts + parts)
    }

    public fun append(part: String, multipart: Boolean = false): SeriesName {
        return if (multipart) {
            val multiparts = part.split('.').map { graphiteSeriesPart(it) }
            SeriesName(parts + multiparts)
        } else {
            SeriesName(parts + graphiteSeriesPart(part))
        }
    }

    public fun append(vararg part: String): SeriesName {
        return SeriesName(parts + part.map { graphiteSeriesPart(it) })
    }

    public fun append(part: SeriesName): SeriesName {
        return part.prefix(this)
    }

    override fun toString(): String {
        return parts.joinToString(separator = ".")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SeriesName

        if (parts != other.parts) return false

        return true
    }

    override fun hashCode(): Int {
        return parts.hashCode()
    }

    public companion object {

        private val invalidSymbols by lazy { "[^a-zA-Z0-9_-]+".toRegex() }

        public fun create(vararg part: String): SeriesName {
            return SeriesName(mutableListOf()).append(*part)
        }

        public fun create(seriesName: String, multipart: Boolean): SeriesName {
            return SeriesName(mutableListOf()).append(seriesName, multipart)
        }

        private fun graphiteSeriesPart(value: String): String {
            return value.replace(invalidSymbols, "_")
        }
    }
}
