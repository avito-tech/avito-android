@file:JvmName("RandomUtils")

package ru.avito.util

import java.util.ArrayList
import java.util.Date
import java.util.Random

// TODO: to consider using reflection for automation.
// For example - https://github.com/mtedone/podam

private val random = Random(System.currentTimeMillis())

/**
 * Returns a pseudo-random uniformly distributed int in the half-open range [1, Int.MAX_VALUE)
 */
fun randomInt(): Int {
    return 1 + random.nextInt(Int.MAX_VALUE - 1)
}

fun randomIntAsString(): String {
    return randomInt().toString()
}

fun randomIntAsString(maxValue: Int): String {
    return randomInt(maxValue).toString()
}

/**
 * Delegate of [Random.nextInt] (int)}
 */
fun randomInt(maxValue: Int): Int {
    return random.nextInt(maxValue)
}

fun randomInt(min: Int, max: Int): Int {
    return min + random.nextInt(max - min + 1)
}

/**
 * Delegate of [Random.nextDouble] ()}
 */
fun randomDouble(): Double {
    return random.nextDouble()
}

fun randomDouble(min: Double, max: Double): Double {
    return min + (max - min) * random.nextDouble()
}

/**
 * Delegate of [Random.nextFloat] ()}
 */
fun randomFloat(): Float {
    return random.nextFloat()
}

/**
 * Delegate of [Random.nextLong] ()}
 */
fun randomLong(): Long {
    return random.nextLong()
}

fun randomLong(min: Long, max: Long): Long {
    return min + ((random.nextDouble() * (max - min))).toLong()
}

/**
 * Delegate of [Random.nextBoolean] ()}
 */
fun randomBoolean(): Boolean {
    return random.nextBoolean()
}

fun randomString(): String {
    return "test_" + randomInt()
}

fun randomUrlString(): String {
    return "https://example.com/" + randomString()
}

fun randomStringList(capacity: Int = randomInt(10)): List<String> {
    val list = ArrayList<String>(capacity)
    for (i in 0 until capacity) {
        list.add(randomString())
    }
    return list
}

fun randomLongArray(): LongArray {
    val array = LongArray(randomInt(10))
    for (i in array.indices) {
        array[i] = randomInt().toLong()
    }
    return array
}

sealed class Characters(val chars: List<Char>) {
    object AlphabeticLowercase : Characters(
        chars = ('a'..'z').toList()
    )

    object AlphabeticUppercase : Characters(
        chars = ('A'..'Z').toList()
    )

    object Alphabetic : Characters(
        chars = AlphabeticLowercase.chars + AlphabeticUppercase.chars
    )

    object Digits : Characters(
        chars = ('0'..'9').toList()
    )

    object Punctuation : Characters(
        chars = listOf('.', ',', '!', '?', ';', ':', '-', '(', ')', '"').toList()
    )

    object Special : Characters(
        chars = listOf('@', '#', '$', '%', '^', '&', '*', '{', '}', '[', ']', '/', '\\', '|')
    )

    object Alphanumeric : Characters(
        chars = Alphabetic.chars + Digits.chars
    )

    object Text : Characters(
        chars = Alphabetic.chars + Digits.chars + Punctuation.chars
    )

    class SingleChar(char: Char) : Characters(listOf(char))

    object All : Characters(
        chars = Alphabetic.chars + Digits.chars + Punctuation.chars + Special.chars
    )
}

fun randomNullableString(): String? {
    val random = Random()
    val isNullable = random.nextInt(3) == 0
    if (isNullable) {
        return null
    } else {
        return randomString()
    }
}

fun randomString(length: Int, symbols: Characters = Characters.Alphanumeric): String {
    val random = Random()
    val builder = StringBuilder()
    repeat(length) {
        val index = random.nextInt(symbols.chars.size)
        builder.append(symbols.chars[index])
    }
    return builder.toString()
}

fun randomIntArray(): IntArray = IntArray(randomInt(10)).map { randomInt() }.toIntArray()

fun randomByteArray(): ByteArray = ByteArray(randomInt(10)).apply { random.nextBytes(this) }

inline fun <reified T : Any> List<T>.randomElement(): T = this[randomInt(this.size)]

inline fun <reified T : Any> Array<T>.randomElement(): T = this[randomInt(this.size)]

fun randomPassword(charLength: Int): String {
    return randomString(charLength, Characters.Alphabetic) + randomInt(10)
}

/**
 * Generates random e-mail.
 * <p>
 * Note: `@avito-test.ru` is needed to allow deleting phones used in registration in functional tests.
 *
 * @return random e-mail address in @avito-test.ru domain
 */
fun randomEmail(): String {
    return randomString(6, Characters.AlphabeticLowercase) + "@avito-test.ru"
}

fun randomPhone(): String {
    return "+7" + randomString(10, Characters.Digits)
}

fun randomDate(start: Long, end: Long): Date {
    return Date(randomLong(start, end))
}
