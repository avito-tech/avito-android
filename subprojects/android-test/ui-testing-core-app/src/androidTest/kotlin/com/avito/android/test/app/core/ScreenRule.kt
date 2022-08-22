package com.avito.android.test.app.core

import android.app.Activity
import com.avito.android.rule.InHouseScenarioScreenRule
import com.avito.android.rule.base.BaseActivityScenarioRule
import org.junit.rules.RuleChain

inline fun <reified T : Activity> screenRule(launchActivity: Boolean = false): GodRuleChain<T> =
    GodRuleChain(
        RuleChain.emptyRuleChain()
            .around(BaseActivityScenarioRule(T::class.java, true, launchActivity))
    )

inline fun <reified T : Activity> inHouseScreenRule() =
    object : InHouseScenarioScreenRule<T>(T::class.java) { }
