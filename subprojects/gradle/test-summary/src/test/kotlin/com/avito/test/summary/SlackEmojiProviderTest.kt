package com.avito.test.summary

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

internal class SlackEmojiProviderTest {

    private val slackEmojiProvider = SlackEmojiProvider()

    @Test
    fun test() {
        assertThat(slackEmojiProvider.emojiName(100)).isEqualTo(":stfgod0:")
        assertThat(slackEmojiProvider.emojiName(110)).isEqualTo(":stfgod0:")
        assertThat(slackEmojiProvider.emojiName(-100)).isEqualTo(":stfdead0:")
        assertThat(slackEmojiProvider.emojiName(10)).isEqualTo(":stfdead0:")
        assertThat(slackEmojiProvider.emojiName(97)).isEqualTo(":stfst01:")
    }
}
