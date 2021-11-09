package com.avito.android.ui.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.RecyclerInRecyclerActivity
import com.avito.truth.checkCausesDeeply
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecyclerInRecyclerTest {

    @get:Rule
    val rule = screenRule<RecyclerInRecyclerActivity>()

    @Test
    fun typedItemAtPosition_foundFirstValue() {
        rule.launchActivity(RecyclerInRecyclerActivity.intent(arrayListOf("0", "1", "2")))

        Screen.recyclerInRecycler.list.horizontalList.cellAt(position = 0)
            .title.checks.displayedWithText("0")
    }

    @Test
    fun typedItemWithMatcher_foundFirstValue() {
        rule.launchActivity(RecyclerInRecyclerActivity.intent(arrayListOf("0", "1", "2")))

        Screen.recyclerInRecycler.list.horizontalList.cellWithTitle("0")
            .title.checks.displayedWithText("0")
    }

    @Test
    fun typedItemAtPosition_foundThirdValue() {
        rule.launchActivity(RecyclerInRecyclerActivity.intent(arrayListOf("0", "1", "2")))

        Screen.recyclerInRecycler.list.horizontalList.cellAt(position = 2)
            .title.checks.displayedWithText("2")
    }

    @Test
    fun typedItemWithMatcher_foundThirdValue() {
        rule.launchActivity(RecyclerInRecyclerActivity.intent(arrayListOf("0", "1", "2")))

        Screen.recyclerInRecycler.list.horizontalList.cellWithTitle("2")
            .title.checks.displayedWithText("2")
    }

    @Test
    fun typedItemWithMatcher_noItem() {
        rule.launchActivity(RecyclerInRecyclerActivity.intent(arrayListOf("0", "1", "2")))

        try {
            Screen.recyclerInRecycler.list.horizontalList.cellWithTitle("3")
                .title.checks.displayedWithText("3")
        } catch (e: Exception) {
            assertThat(e)
                .checkCausesDeeply {
                    isInstanceOf(AssertionError::class.java)
                    hasMessageThat()
                        .contains("No item in recycler. Recycler size: 3")
                }
        }
    }

    @Test
    fun typedItemWithMatcher_noItemAtPosition() {
        rule.launchActivity(RecyclerInRecyclerActivity.intent(arrayListOf("0", "1", "2")))

        try {
            Screen.recyclerInRecycler.list.horizontalList.cellWithTitle("2", 0)
                .title.checks.displayedWithText("3")
        } catch (e: Exception) {
            assertThat(e)
                .checkCausesDeeply {
                    isInstanceOf(AssertionError::class.java)
                    hasMessageThat()
                        .apply {
                            contains("No matched item in recycler at position 0 but was")
                            contains("Search near items from 0 to 3 has matches at positions: [2]")
                        }
                }
        }
    }

    @Test
    fun typedItemWithMatcher_indexOutOfBound() {
        rule.launchActivity(RecyclerInRecyclerActivity.intent(arrayListOf("0", "1", "2")))

        try {
            Screen.recyclerInRecycler.list.horizontalList.cellAt(3)
                .title.checks.displayedWithText("3")
        } catch (e: Exception) {
            assertThat(e)
                .checkCausesDeeply {
                    isInstanceOf(AssertionError::class.java)
                    hasMessageThat()
                        .contains("Tried to match item at position 3. But recycler size is 3")
                }
        }
    }
}
