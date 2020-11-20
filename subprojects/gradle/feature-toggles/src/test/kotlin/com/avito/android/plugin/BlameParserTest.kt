package com.avito.android.plugin

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId

class BlameParserTest {

    private lateinit var blameParser: BlameParser

    @BeforeEach
    fun setUp() {
        blameParser = BlameParser()
    }

    @Test
    fun `read blame - parse values`() {
        val values = listOf(
            CodeElement(
                " package com.avito.android",
                changeTime = "1455703948".toLocalDate(),
                email = "dkostyrev@avito.ru"
            ),
            CodeElement(
                "",
                changeTime = "1455703948".toLocalDate(),
                email = "dkostyrev@avito.ru"
            ),
            CodeElement(
                " import com.avito.android.toggle.Feature",
                changeTime = "1484325718".toLocalDate(),
                email = "ekrivobokov@avito.ru"
            )
        )

        val lines = blameParser.parseBlameCodeLines(correctBlame)

        assertThat(values).isEqualTo(lines)
    }

    fun String.toLocalDate() = Instant.ofEpochSecond(this.toLong()).atZone(ZoneId.of("Europe/Moscow")).toLocalDate()
}

@Suppress("MaxLineLength")
private const val correctBlame =
    """f738698526f avito/src/main/kotlin/com/avito/android/Features.kt  (<dkostyrev@avito.ru>     1455703948 +0300   1) package com.avito.android
f738698526f avito/src/main/kotlin/com/avito/android/Features.kt  (<dkostyrev@avito.ru>     1455703948 +0300   2)
a83712b013e avito/src/main/java/com/avito/android/Features.kt    (<ekrivobokov@avito.ru>   1484325718 +0300   3) import com.avito.android.toggle.Feature"""
