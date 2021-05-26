package com.avito.android.ui.test

import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.SwipeRefreshActivity
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class SwipeRefreshTest {

    @get:Rule
    val rule = screenRule<SwipeRefreshActivity>()

    @Test
    fun refresh_onPullToRefresh_listAction() {
        rule.launchActivity(null)

        Screen.swipeRefresh.list.actions.pullToRefresh()

        Screen.swipeRefresh.swipeRefreshElement.checks.isRefreshing()
        rule.onActivity {
            assertThat(refreshedTimes).isEqualTo(1)
        }
    }

    @Test
    fun refresh_onPullToRefresh_swipeRefreshAction() {
        rule.launchActivity(null)

        Screen.swipeRefresh.swipeRefreshElement.actions.pullToRefresh()

        rule.onActivity {
            assertThat(refreshedTimes).isEqualTo(1)
        }
    }

    @Test
    fun stop_refreshing_by_intention() {
        rule.launchActivity(null)

        Screen.swipeRefresh.swipeRefreshElement.actions.pullToRefresh()

        rule.onActivity {
            postAndStopRefreshing()
        }

        Screen.swipeRefresh.swipeRefreshElement.checks.isNotRefreshing()
    }
}
