@file:Suppress("unused")

package com.avito.android.rule

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.avito.android.util.waitForAssertion
import com.google.common.truth.Truth.assertThat
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Screen rule for testing a single Fragment destination within the single-activity architecture.
 *
 * Replaces [InHouseScenarioScreenRule] for screens that have been migrated from dedicated
 * Activities to Fragment destinations in the Jetpack Navigation graph.
 *
 * Inherit your screen rule:
 *
 * class MyFeatureScreenRule : InHouseFragmentScenarioScreenRule<NavigationActivity>(
 *    activityClass = NavigationActivity::class.java,
 *    navHostId = R.id.nav_host_fragment,
 *    startDestinationKey = "start_destination_key",
 * )
 *
 * In your test:
 *
 * class MyFeatureTest {
 *
 *     @get:Rule
 *     val screenRule = MyFeatureScreenRule()
 *
 *     @Test
 *     fun someTest() {
 *         screenRule.launchFragment(destination = ProductDetailRoute(productId = 1))
 *         // ...
 *     }
 * }
 */
public abstract class InHouseFragmentScenarioScreenRule<A : AppCompatActivity>(
    activityClass: Class<A>,
    @param:IdRes private val navHostId: Int,
    private val startDestinationKey: String,
    stubIntents: Boolean = true,
) : TestRule {

    private val activityRule = ActivityScenarioRule(activityClass, stubIntents)

    private var fragmentResult: Bundle? = null

    protected val appContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

    protected val testContext: Context
        get() = InstrumentationRegistry.getInstrumentation().context

    public val scenario: ActivityScenario<A>
        get() = activityRule.scenario

    public val checks: ChecksLibrary<A> = ChecksLibrary(navHostId, { scenario }, { fragmentResult })

    /**
     * Launches the host activity and navigates directly to the [destination].
     *
     * @param startIntent optional custom Intent to launch the activity with, e.g. to carry
     *   extras that NavigationActivity needs before the nav graph is inflated.
     */
    public fun launchFragment(
        destination: Parcelable,
        startIntent: Intent? = null,
    ): ActivityScenario<A> {
        val intent = (startIntent ?: Intent()).putExtra(startDestinationKey, destination)
        activityRule.launchActivity(intent)
        return scenario
    }

    /**
     * Sets fragment result listener for the provided [requestKey].
     * Should be called in case the result is set using Fragment Result API.
     */
    public fun setFragmentResultListener(requestKey: String) {
        scenario.onActivity { activity ->
            activity.navHost().childFragmentManager.setFragmentResultListener(requestKey, activity) { _, bundle ->
                fragmentResult = bundle
            }
        }
    }

    private fun A.navHost(): NavHostFragment = supportFragmentManager.findFragmentById(navHostId) as NavHostFragment

    override fun apply(base: Statement, description: Description): Statement =
        activityRule.apply(base, description)

    public class ChecksLibrary<A : AppCompatActivity>(
        @param:IdRes private val navHostId: Int,
        private val scenarioFunc: () -> ActivityScenario<A>,
        private val fragmentResultFunc: () -> Bundle?,
    ) {

        /**
         * Checks a result passed back via NavBackStackEntry SavedStateHandle.
         */
        public fun <T> hasBackStackResult(key: String, expectedValue: T) {
            waitForAssertion {
                var actualValue: T? = null
                scenarioFunc().onActivity { activity ->
                    actualValue = activity.navController()
                        .currentBackStackEntry
                        ?.savedStateHandle
                        ?.get<T>(key)
                }
                assertThat(actualValue).isEqualTo(expectedValue)
            }
        }

        /**
         * Asserts a result set via [FragmentManager.setFragmentResult].
         * Useful when the fragment uses the Fragment Result API directly
         * rather than SavedStateHandle.
         *
         * [setFragmentResultListener] must be called before the assertion.
         */
        public fun assertFragmentResult(assertion: (Bundle) -> Unit) {
            waitForAssertion {
                val capturedBundle = fragmentResultFunc()
                assertThat(capturedBundle).isNotNull()
                assertion(capturedBundle!!)
            }
        }

        private fun A.navController(): NavController =
            (supportFragmentManager.findFragmentById(navHostId) as NavHostFragment).navController
    }
}
