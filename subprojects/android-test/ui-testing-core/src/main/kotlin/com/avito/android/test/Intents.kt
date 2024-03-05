package com.avito.android.test

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.util.Log
import androidx.test.core.app.hasInstrumentationActivityComponent
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.OngoingStubbing
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.UriMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher

public object Intents {

    public val checks: Checks = Checks()

    public fun resultOK(resultData: Intent? = null): Instrumentation.ActivityResult =
        Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

    public fun resultCanceled(resultData: Intent? = null): Instrumentation.ActivityResult =
        Instrumentation.ActivityResult(Activity.RESULT_CANCELED, resultData)

    public inline fun <reified T : Activity> whenIntended(): OngoingStubbing =
        Intents.intending(IntentMatchers.hasComponent(T::class.qualifiedName))

    public fun stubEverything(): Unit =
        Intents.intending(NotInstrumentationIntentMatcher()).respondWithFunction { intent: Intent ->
            val result = resultCanceled()
            Log.d("Intents", "Responding to $intent with ${result.toReadableString()}")
            result
        }

    private fun Instrumentation.ActivityResult.toReadableString() =
        "ActivityResult { resultCode=${this.resultCode} resultData=${this.resultData} }"

    /**
     * TODO: Remove after MBS-11523
     * Test will freeze in the end if instrumentation intents are stubbed.
     * {@link androidx.test.core.app.InstrumentationActivityInvoker#finishActivity finishActivity}
     */
    private class NotInstrumentationIntentMatcher : TypeSafeMatcher<Intent>() {
        override fun describeTo(description: Description) {
            description.appendText("not instrumentation intent")
        }

        override fun matchesSafely(item: Intent): Boolean {
            return !item.hasInstrumentationActivityComponent()
        }
    }

    public class Checks {

        public fun actionIntended(action: String, vararg additionalMatchers: Matcher<Intent>) {
            Intents.intended(Matchers.allOf(IntentMatchers.hasAction(action), *additionalMatchers))
        }

        public fun activityIntended(className: String): Unit = waitFor {
            Intents.intended(IntentMatchers.hasComponent(className))
        }

        public inline fun <reified T : Activity> activityIntended(
            vararg additionalMatchers: Matcher<Intent>
        ): Unit = waitFor {
            Intents.intended(
                Matchers.allOf(
                    IntentMatchers.hasComponent(T::class.java.name),
                    *additionalMatchers
                )
            )
        }

        public fun actionIntended(action: String, key: String, value: String) {
            actionIntended(action, IntentMatchers.hasExtra(key, value))
        }

        public fun actionIntendedWithUriParam(action: String, param: String, value: String) {
            actionIntended(
                action,
                IntentMatchers.hasData(UriMatchers.hasParamWithValue(param, value))
            )
        }

        public inline fun <reified T : Activity> activityIntendedWithUriParam(
            param: String,
            value: String
        ) {
            activityIntended<T>(IntentMatchers.hasData(UriMatchers.hasParamWithValue(param, value)))
        }

        public inline fun <reified T : Activity> activityIntendedWithoutExtraParam(param: String) {
            activityIntended<T>(Matchers.not(IntentMatchers.hasExtraWithKey(param)))
        }

        public inline fun <reified T : Activity> activityIntendedWithExtraParam(
            param: String,
            value: String
        ) {
            activityIntended<T>(IntentMatchers.hasExtra(param, value))
        }
    }
}
