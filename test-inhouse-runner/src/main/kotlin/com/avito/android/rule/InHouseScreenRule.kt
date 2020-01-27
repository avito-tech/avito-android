package com.avito.android.rule

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.util.waitForAssertion
import org.junit.Assert
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Обертка для работы с активити
 * принудительно не запускаем активити сразу
 *
 * Наследуемся в классах **Screen
 *
 * class **Screen {
 *
 *   ...
 *
 *   class Rule : ScreenRule<PhoneVerificationActivity>(PhoneVerificationActivity::class.java) {
 *
 *     fun startWithPhone(phone: String) {
 *       launchActivity(PhoneVerificationActivity.intent(context, phone))
 *     }
 *   }
 * }
 *
 * В тесте:
 *
 * class PhoneVerificationTest {
 *
 *   @get:Rule
 *   val screenRule = PhoneVerificationScreen.Rule()
 *
 *   @Test
 *   fun successStart() {
 *
 *     screenRule.startWithPhone(phone)
 *   }
 * }
 */
abstract class InHouseScreenRule<T : Activity>(activityClass: Class<T>) : TestRule {

    protected val androidInstrumentation: Instrumentation = InstrumentationRegistry.getInstrumentation()
    protected val arguments: Bundle = InstrumentationRegistry.getArguments()
    protected val appContext: Context = androidInstrumentation.targetContext.applicationContext
    protected val testContext: Context = androidInstrumentation.context

    private val activityRule = ActivityRule(activityClass)

    val checks = ChecksLibrary { activityRule.activity }

    val activityResult: Instrumentation.ActivityResult
        get() = activityRule.activityResult

    val activity: Activity
        get() = activityRule.activity

    fun launchActivity(startIntent: Intent?): T = activityRule.launchActivity(startIntent)

    fun runOnUiThread(body: () -> Unit) {
        activityRule.runOnUiThread { body.invoke() }
    }

    class ChecksLibrary(private val activity: () -> Activity?) {

        fun isFinishing() {
            waitForAssertion { Assert.assertTrue(activity()?.isFinishing ?: false) }
        }

        fun isNotFinishing() {
            waitForAssertion { Assert.assertFalse(activity()?.isFinishing ?: true) }
        }

        fun activityResult(expectedResult: Int) {
            val field = Activity::class.java.getDeclaredField("mResultCode")
            field.isAccessible = true
            val actualResult = field.getInt(activity())
            val errorMessage = "Activity result code mismatch\n" +
                "expected: $expectedResult\n" +
                "actual: $actualResult"

            waitForAssertion { Assert.assertTrue(errorMessage, expectedResult == actualResult) }
        }
    }

    override fun apply(base: Statement, description: Description): Statement = activityRule.apply(base, description)
}
