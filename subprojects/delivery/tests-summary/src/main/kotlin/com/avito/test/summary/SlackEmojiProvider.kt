package com.avito.test.summary

internal class SlackEmojiProvider {

    fun emojiName(health: Int): String = when (health) {
        in 100..Int.MAX_VALUE -> ":stfgod0:"
        in 95..99 -> ":stfst01:"
        in 85..94 -> ":stfst11:"
        in 75..84 -> ":stfst21:"
        in 65..74 -> ":stfst31:"
        in 50..64 -> ":stfst41:"
        in 0..49 -> ":stfdead0:"
        else -> ":stfdead0:"
    }
}
