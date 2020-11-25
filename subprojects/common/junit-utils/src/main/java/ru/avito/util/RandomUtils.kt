@file:JvmName("RandomUtils")

package ru.avito.util

import java.util.ArrayList
import java.util.Date
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

// TODO: to consider using reflection for automation.
// For example - https://github.com/mtedone/podam

private val random: ThreadLocalRandom
    get() = ThreadLocalRandom.current()

private const val DEFAULT_MAX_INT = 10

/**
 * Returns a pseudo-random uniformly distributed int in the half-open range [0, Int.MAX_VALUE)
 */
fun randomInt(): Int {
    return random.nextInt(Int.MAX_VALUE)
}

fun randomIntAsString(): String {
    return randomInt().toString()
}

fun randomIntAsString(maxValue: Int): String {
    return randomInt(maxValue).toString()
}

/**
 * Delegate of [ThreadLocalRandom.nextInt] (int)}
 */
fun randomInt(maxValue: Int): Int {
    return random.nextInt(maxValue)
}

fun randomInt(min: Int, max: Int): Int {
    require(max >= min) {
        "`min` must be lower or equal then `max`"
    }
    return min + random.nextInt(max - min + 1)
}

/**
 * Delegate of [ThreadLocalRandom.nextDouble] ()}
 */
fun randomDouble(): Double {
    return random.nextDouble()
}

fun randomDouble(min: Double, max: Double): Double {
    return random.nextDouble(min, max)
}

/**
 * Delegate of [ThreadLocalRandom.nextFloat] ()}
 */
fun randomFloat(): Float {
    return random.nextFloat()
}

/**
 * @return value [0, Long.MAX_VALUE)
 */
fun randomLong(): Long {
    return randomLong(min = 0, max = Long.MAX_VALUE)
}

fun randomLong(min: Long, max: Long): Long {
    require(max >= min) {
        "`min` must be lower or equal then `max`"
    }
    return random.nextLong(min, max)
}

/**
 * Delegate of [ThreadLocalRandom.nextBoolean] ()}
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

fun randomStringList(capacity: Int = randomInt(DEFAULT_MAX_INT)): List<String> {
    val list = ArrayList<String>(capacity)
    for (i in 0 until capacity) {
        list.add(randomString())
    }
    return list
}

fun randomLongArray(): LongArray {
    val array = LongArray(randomInt(DEFAULT_MAX_INT))
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

@Suppress("MagicNumber")
fun randomNullableString(): String? {
    val random = Random()
    val isNullable = random.nextInt(3) == 0
    return if (isNullable) {
        null
    } else {
        randomString()
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

fun randomIntArray(): IntArray = IntArray(randomInt(DEFAULT_MAX_INT)).map { randomInt() }.toIntArray()

fun randomByteArray(): ByteArray = ByteArray(randomInt(DEFAULT_MAX_INT)).apply { random.nextBytes(this) }

inline fun <reified T : Any> List<T>.randomElement(): T = this[randomInt(this.size)]

inline fun <reified T : Any> Array<T>.randomElement(): T = this[randomInt(this.size)]

fun randomPassword(charLength: Int): String {
    return randomString(charLength, Characters.Alphabetic) + randomInt(DEFAULT_MAX_INT)
}

/**
 * Generates random e-mail.
 * <p>
 * Note: `@avito-test.ru` is needed to allow deleting phones used in registration in functional tests.
 *
 * @return random e-mail address in @avito-test.ru domain
 */
@Suppress("MagicNumber")
fun randomEmail(): String {
    return randomString(6, Characters.AlphabeticLowercase) + "@avito-test.ru"
}

@Suppress("MagicNumber")
fun randomPhone(): String {
    return "+7" + randomString(10, Characters.Digits)
}

@Deprecated("start and end could be negative", replaceWith = ReplaceWith("randomDate(start, end)"))
fun randomDate(start: Long, end: Long): Date {
    return Date(randomLong(start, end))
}

fun randomDate(start: Date, end: Date): Date {
    return Date(randomLong(start.time, end.time))
}

fun randomDate(): Date = Date(randomLong(min = 0, max = Long.MAX_VALUE))

fun randomDateInMillis(): Long = randomDate().time
