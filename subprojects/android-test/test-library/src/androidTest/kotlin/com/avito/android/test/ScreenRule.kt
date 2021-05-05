package com.avito.android.test

import android.app.Activity
import com.avito.android.rule.ActivityScenarioRule
import org.junit.rules.RuleChain

inline fun <reified T : Activity> screenRule(launchActivity: Boolean = false): GodRuleChain<T> =
    GodRuleChain(
        RuleChain.emptyRuleChain()
            .around(ActivityScenarioRule(T::class.java, launchActivity))
    )
