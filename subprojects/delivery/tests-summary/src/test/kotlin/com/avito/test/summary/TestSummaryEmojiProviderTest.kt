package com.avito.test.summary

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class TestSummaryEmojiProviderTest {

    private val testSummaryEmojiProvider = TestSummaryEmojiProvider()

    @Test
    fun test() {
        assertThat(testSummaryEmojiProvider.emojiName(100)).isEqualTo(":stfgod0:")
        assertThat(testSummaryEmojiProvider.emojiName(110)).isEqualTo(":stfgod0:")
        assertThat(testSummaryEmojiProvider.emojiName(-100)).isEqualTo(":stfdead0:")
        assertThat(testSummaryEmojiProvider.emojiName(10)).isEqualTo(":stfdead0:")
        assertThat(testSummaryEmojiProvider.emojiName(97)).isEqualTo(":stfst01:")
    }
}
