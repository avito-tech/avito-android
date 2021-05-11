package com.avito.runner.test

import com.avito.coroutines.extensions.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher

@ExperimentalCoroutinesApi
object TestDispatcher : Dispatchers {
    override fun dispatcher(): CoroutineDispatcher {
        return TestCoroutineDispatcher()
    }
}
