package com.avito.android.ui.test

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.StatefulRecyclerViewAdapterActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("MaxLineLength")
@RunWith(AndroidJUnit4::class)
class StatefulRecyclerViewAdapterTest {

    @get:Rule
    val rule = screenRule<StatefulRecyclerViewAdapterActivity>(launchActivity = false)

    /**
     * For finding element inside recycler view by view matching we have to render all items
     * inside recycler view. That rendering may affect application logic (production code may
     * cause some side effects inside recycler view item binding code).
     *
     * We can't avoid that behavior. For that reason, we've created special tag for
     * ViewHolder itemView, that can be used by production code for detecting fake rendering
     * and avoiding any side effects.
     *
     * For more details look at recycler view adapter inside StatefulRecyclerViewAdapterActivity
     */
    @Test
    fun adapterState_notAffectedByDynamicRecyclerViewElementFindingLogic_whenProductionCodeAvoidToUsingSideEffectsForFakeViewHolder() {
        rule.launchActivity(Intent())

        with(Screen.statefulRecyclerViewAdapterScreen.list) {
            cellWithTitle("60").scrollTo()
            cellWithTitle("1").scrollTo()
            cellWithTitle("60").scrollTo()
            cellWithTitle("1").scrollTo()

            cellWithTitle("1").title2.checks.displayedWithText("3")
        }
    }

    @Test
    fun adapterState_affectedByDynamicRecyclerViewElementFindingLogic_onlyOnceForOneScrollEvent() {
        rule.launchActivity {
            it.apply {
                putExtra(
                    StatefulRecyclerViewAdapterActivity.CHANGE_STATE_FOR_TEST_BINDINGS_KEY,
                    true
                )
            }
        }

        var expectedBindingsCount = 0

        with(Screen.statefulRecyclerViewAdapterScreen.list) {
            cellWithTitle("60").scrollTo().apply {
                expectedBindingsCount++ // fake binding for finding element
                expectedBindingsCount++ // real binding for showing element
            }
            cellWithTitle("1").scrollTo()
            cellWithTitle("60").scrollTo().apply {
                expectedBindingsCount++ // fake binding for finding element
                expectedBindingsCount++ // real binding for showing element
            }

            cellWithTitle("60").title2.checks.displayedWithText(expectedBindingsCount.toString())
        }
    }

    @Test
    fun adapterState_affectedByDynamicRecyclerViewElementFindingLogic_onlyOnceForOneClickEvent() {
        rule.launchActivity {
            it.apply {
                putExtra(
                    StatefulRecyclerViewAdapterActivity.CHANGE_STATE_FOR_TEST_BINDINGS_KEY,
                    true
                )
            }
        }

        var expectedBindingsCount = 0

        with(Screen.statefulRecyclerViewAdapterScreen.list) {
            cellWithTitle("60").click().apply {
                expectedBindingsCount++ // fake binding for finding element
                expectedBindingsCount++ // real binding for showing element
            }
            cellWithTitle("1").click()
            cellWithTitle("60").click().apply {
                expectedBindingsCount++ // fake binding for finding element
                expectedBindingsCount++ // real binding for showing element
            }

            cellWithTitle("60").title2.checks.displayedWithText(expectedBindingsCount.toString())
        }
    }

    @Test
    fun adapterState_affectedByDynamicRecyclerViewElementFindingLogic_onlyOnceForOneClickEventWithItemsCreatedWithRecyclerViewInteractionContext() {
        rule.launchActivity {
            it.apply {
                putExtra(
                    StatefulRecyclerViewAdapterActivity.CHANGE_STATE_FOR_TEST_BINDINGS_KEY,
                    true
                )
            }
        }

        var expectedBindingsCount = 0

        with(Screen.statefulRecyclerViewAdapterScreen.list) {
            cellWithTitleCreatedByRecyclerViewInteractionContext("60").click()
                .apply {
                    expectedBindingsCount++ // fake binding for finding element
                    expectedBindingsCount++ // real binding for showing element
                }
            cellWithTitleCreatedByRecyclerViewInteractionContext("1").click()
            cellWithTitleCreatedByRecyclerViewInteractionContext("60").click()
                .apply {
                    expectedBindingsCount++ // fake binding for finding element
                    expectedBindingsCount++ // real binding for showing element
                }

            cellWithTitle("60").title2.checks.displayedWithText(expectedBindingsCount.toString())
        }
    }

    @Test
    fun adapterState_doesNotAffectedByDynamicRecyclerViewElementFindingLogic_whenItemAlreadyOnTheScreen() {
        rule.launchActivity {
            it.apply {
                putExtra(
                    StatefulRecyclerViewAdapterActivity.CHANGE_STATE_FOR_TEST_BINDINGS_KEY,
                    true
                )
            }
        }

        var expectedBindingsCount = 0

        expectedBindingsCount++ // initial binding

        with(Screen.statefulRecyclerViewAdapterScreen.list) {

            cellWithTitleCreatedByRecyclerViewInteractionContext("1").click()

            // there is no fake binding because item already on the screen
            cellWithTitleCreatedByRecyclerViewInteractionContext("60").click()
            cellWithTitleCreatedByRecyclerViewInteractionContext("1").click()
                .apply {
                    expectedBindingsCount++ // fake binding for finding element
                    expectedBindingsCount++ // real binding for showing element
                }
            cellWithTitleCreatedByRecyclerViewInteractionContext("1").click()

            cellWithTitle("1").title2.checks.displayedWithText(expectedBindingsCount.toString())
        }
    }
}
