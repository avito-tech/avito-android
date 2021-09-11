package com.avito.android.ui.test.launch

import org.junit.Rule
import org.junit.Test

class LaunchingTest {

    @get:Rule
    val rule = LaunchRule()

    @Test
    fun launchFromHomeScreen__success() {
        rule.startFromHomeScreen()
    }
}
