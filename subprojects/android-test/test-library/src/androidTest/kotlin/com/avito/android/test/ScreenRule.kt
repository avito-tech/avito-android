package com.avito.android.test

import android.app.Activity
import androidx.test.rule.ActivityTestRule
import org.junit.rules.RuleChain

inline fun <reified T : Activity> screenRule(launchActivity: Boolean = false): GodRuleChain<T> =
    GodRuleChain(
        RuleChain.emptyRuleChain()
            .around(ActivityTestRule(T::class.java, true, launchActivity))
    )
