package com.avito.test

import kotlinx.coroutines.channels.ReceiveChannel

suspend fun <E> ReceiveChannel<E>.receiveAvailable(): List<E> {
    val allMessages = mutableListOf<E>()

    allMessages.add(receive())

    var next = poll()
    while (next != null) {
        allMessages.add(next)
        next = poll()
    }

    return allMessages
}
