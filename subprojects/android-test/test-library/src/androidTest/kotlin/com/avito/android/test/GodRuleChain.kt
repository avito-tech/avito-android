package com.avito.android.test

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.rule.ActivityTestRule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * RuleChain with exposed methods and fields from it's rules
 */
class GodRuleChain<out T : Activity>(private val chain: RuleChain) :
    TestRule {

    @Suppress("UNCHECKED_CAST")
    private val rules = chain.javaClass.getDeclaredField("rulesStartingWithInnerMost")
        .apply { isAccessible = true }.get(chain) as List<TestRule>

    private val activityTestRule = rules.filterIsInstance<ActivityTestRule<T>>().first()

    val activity: T
        get() = activityTestRule.activity

    val activityResult: Instrumentation.ActivityResult
        get() = activityTestRule.activityResult

    fun runOnUiThread(runnable: () -> Unit) = activityTestRule.runOnUiThread(runnable)

    fun launchActivity(intent: Intent?): T = activityTestRule.launchActivity(intent)

    fun launchActivity(func: (Intent) -> Intent): T =
        activityTestRule.launchActivity(func(Intent(Intent.ACTION_MAIN)))

    override fun apply(base: Statement?, description: Description?): Statement {
        return chain.apply(base, description)
    }
}
