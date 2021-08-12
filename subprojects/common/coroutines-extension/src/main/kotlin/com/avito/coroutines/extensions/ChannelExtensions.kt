package com.avito.coroutines.extensions

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel

@ExperimentalCoroutinesApi
public val <E> Channel<E>.isClosedForSendAndReceive: Boolean
    get() = this.isClosedForSend && this.isClosedForReceive
