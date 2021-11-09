package com.avito.android.ui.test

import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.R
import com.avito.android.ui.RecyclerWithLongItemsActivity
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecyclerWithSingleLongItemTest {

    @get:Rule
    val rule = screenRule<RecyclerWithLongItemsActivity>()

    @Test
    fun single_reversed_top() {
        test(
            DataSet(
                reversed = true,
                targetViews = listOf(R.id.target_view_top)
            )
        )
    }

    @Test
    fun single_top() {
        test(
            DataSet(
                reversed = false,
                targetViews = listOf(R.id.target_view_top)
            )
        )
    }

    @Test
    fun single_reversed_bottom() {
        test(
            DataSet(
                reversed = true,
                targetViews = listOf(R.id.target_view_bottom)
            )
        )
    }

    @Test
    fun single_bottom() {
        test(
            DataSet(
                reversed = false,
                targetViews = listOf(R.id.target_view_bottom)
            )
        )
    }

    @Test
    fun multiple() {
        test(
            DataSet(
                reversed = false,
                targetViews = listOf(
                    R.id.target_view_top,
                    R.id.target_view_center,
                    R.id.target_view_bottom
                )
            )
        )
    }

    @Test
    fun multiple_reversed() {
        test(
            DataSet(
                reversed = true,
                targetViews = listOf(
                    R.id.target_view_top,
                    R.id.target_view_center,
                    R.id.target_view_bottom
                )
            )
        )
    }

    @Test
    fun multiple_kek() {
        test(
            DataSet(
                reversed = false,
                targetViews = listOf(
                    R.id.target_view_center,
                    R.id.target_view_bottom,
                    R.id.target_view_top
                )
            )
        )
    }

    private fun test(dataSet: DataSet) {
        rule.launchActivity(RecyclerWithLongItemsActivity.intent(dataSet.reversed))

        with(Screen.recyclerWithSingleLongItemScreen.list) {
            for (targetView in dataSet.targetViews.shuffled()) {
                actions.actionOnChild(
                    position = 0,
                    targetChildViewId = targetView,
                    childMatcher = allOf(withId(targetView), isDisplayed()),
                    action = ViewActions.click()
                )
            }
        }
    }

    data class DataSet(
        val reversed: Boolean,
        val targetViews: List<Int>
    )
}
