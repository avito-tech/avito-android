package com.avito.runner.test

import java.util.Random

private val random = Random(System.currentTimeMillis())

fun randomInt(): Int {
    return random.nextInt(Integer.MAX_VALUE - 1)
}

fun randomLong(): Long {
    return random.nextLong()
}

fun randomString(): String {
    return "test_" + randomInt()
}
