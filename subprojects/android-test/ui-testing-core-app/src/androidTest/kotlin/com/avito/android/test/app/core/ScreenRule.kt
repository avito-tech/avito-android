package com.avito.android.test.app.core

import android.app.Activity
import com.avito.android.rule.InHouseScenarioScreenRule
import com.avito.android.rule.base.BaseActivityScenarioRule
import org.junit.rules.RuleChain

inline fun <reified T : Activity> screenRule(launchActivity: Boolean = false): GodRuleChain<T> =
    GodRuleChain(
        RuleChain.emptyRuleChain()
            .around(ScreenRule(T::class.java, launchActivity))
    )

class ScreenRule<A : Activity>(
    activityClass: Class<A>,
    launchActivity: Boolean,
) : BaseActivityScenarioRule<A>(
    activityClass, true, launchActivity,
) {
    override fun afterActivityLaunched() {
        // empty
    }

    override fun afterActivityFinished() {
        // empty
    }
}

inline fun <reified T : Activity> inHouseScreenRule() =
    object : InHouseScenarioScreenRule<T>(T::class.java) {}
