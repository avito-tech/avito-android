package com.avito.android.impact

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class ReadSymbolListKtTest {

    @Test
    fun `readSymbolList - returns name-id pair - from diverse input`() {
        val result = readSymbolList(
            sequenceOf(
                "int drawable vip 0x7f080482",
                "int id address 0x7f0a005c",
                "int styleable AppBarLayout_expanded 4",
                "int[] styleable ViewBackgroundHelper { 0x010100d4, 0x7f04005d, 0x7f04005e }"
            )
        )

        assertThat(result).containsEntry("address", 2131361884)
    }

    @Test
    fun `readSymbolList - returns emptyMap - for empty input`() {
        val result = readSymbolList(emptySequence())

        assertThat(result).isEmpty()
    }
}
