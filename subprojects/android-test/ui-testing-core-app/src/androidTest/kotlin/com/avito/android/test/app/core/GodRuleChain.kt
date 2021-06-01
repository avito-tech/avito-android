package com.avito.android.test.app.core

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import com.avito.android.rule.base.BaseActivityScenarioRule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * RuleChain with exposed methods and fields from it's rules
 */
class GodRuleChain<T : Activity>(private val chain: RuleChain) : TestRule {

    @Suppress("UNCHECKED_CAST")
    private val rules = chain.javaClass.getDeclaredField("rulesStartingWithInnerMost")
        .apply { isAccessible = true }.get(chain) as List<TestRule>

    private val activityTestRule = rules.filterIsInstance<BaseActivityScenarioRule<T>>().first()

    fun onActivity(func: T.() -> Unit): ActivityScenario<T> = activityTestRule.scenario.onActivity { func(it) }

    fun launchActivity(intent: Intent?): ActivityScenario<T> = activityTestRule.launchActivity(intent)

    fun launchActivity(func: (Intent) -> Intent): ActivityScenario<T> =
        activityTestRule.launchActivity(func(Intent(Intent.ACTION_MAIN)))

    override fun apply(base: Statement, description: Description): Statement {
        return chain.apply(base, description)
    }
}
