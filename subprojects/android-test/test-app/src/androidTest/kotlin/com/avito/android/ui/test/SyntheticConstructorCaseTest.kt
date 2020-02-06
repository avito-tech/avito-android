package com.avito.android.ui.test

import com.avito.android.ui.SyntheticConstructorCase
import com.avito.android.ui.VisibilityActivity
import org.junit.Rule
import org.junit.Test

class SyntheticConstructorCaseTest {

    @get:Rule
    val rule = screenRule<VisibilityActivity>()

    @Test
    fun syntheticConstructorCall() {
        rule.launchActivity(null)

        SyntheticConstructorCase.Two(reason = "xxx").toString()
    }
}
