package com.avito.emcee

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun packageNameIsCorrect() {
        assertEquals("com.avito.emcee", InstrumentationRegistry.getInstrumentation().targetContext.packageName)
    }

    @Test
    fun thisIsExampleInstrumentedTestClass() {
        assertEquals("ExampleInstrumentedTest", this::class.simpleName)
    }
}
