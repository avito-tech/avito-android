package com.avito.graphite.series

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
    private val parts: List<String>,
    private val tags: Map<String, String>,
) : Serializable {

    public fun addTag(key: String, value: String): SeriesName {
        requireTag(key, value)
        return SeriesName(parts, tags.plus(key to value))
    }

    public fun addTags(tags: Map<String, String>): SeriesName {
        tags.forEach { (k, v) -> requireTag(k, v) }
        return SeriesName(parts, tags.plus(tags))
    }

    public fun prefix(seriesName: SeriesName): SeriesName {
        return SeriesName(seriesName.parts + parts, seriesName.tags + tags)
    }

    public fun append(part: String, multipart: Boolean = false): SeriesName {
        return if (multipart) {
            val multiparts = part.split('.').map { graphiteSeriesPart(it) }
            SeriesName(parts + multiparts, tags)
        } else {
            SeriesName(parts + graphiteSeriesPart(part), tags)
        }
    }

    public fun append(vararg part: String): SeriesName {
        return SeriesName(parts + part.map { graphiteSeriesPart(it) }, tags)
    }

    public fun append(part: SeriesName): SeriesName {
        return part.prefix(this)
    }

    public fun asAspect(): String = buildString {
        append(parts.joinToString(separator = "."))
        if (tags.isNotEmpty()) {
            append(tags.map { "${it.key}=${it.value}" }.joinToString(prefix = ";", separator = ";"))
        }
    }

    private fun requireTag(key: String, value: String) {
        require(key.isNotBlank()) {
            "Tag key mustn't be blank"
        }
        require(value.isNotBlank()) {
            "Tag value mustn't be blank"
        }
    }

    override fun toString(): String = asAspect()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SeriesName

        if (parts != other.parts) return false

        if (tags != other.tags) return false

        return true
    }

    override fun hashCode(): Int {
        return 31 * parts.hashCode() + tags.hashCode()
    }

    public companion object {

        private val invalidSymbols by lazy { "[^a-zA-Z0-9_-]+".toRegex() }

        public fun create(vararg part: String): SeriesName {
            return SeriesName(mutableListOf(), emptyMap()).append(*part)
        }

        public fun create(seriesName: String, multipart: Boolean): SeriesName {
            return SeriesName(mutableListOf(), emptyMap()).append(seriesName, multipart)
        }

        private fun graphiteSeriesPart(value: String): String {
            return value.replace(invalidSymbols, "_")
        }
    }
}
