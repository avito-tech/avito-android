package com.avito.android.ui.test

import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.SwipeRefreshActivity
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
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
        assertThat(rule.activity.refreshedTimes, equalTo(1))
    }

    @Test
    fun refresh_onPullToRefresh_swipeRefreshAction() {
        rule.launchActivity(null)

        Screen.swipeRefresh.swipeRefreshElement.actions.pullToRefresh()

        assertThat(rule.activity.refreshedTimes, equalTo(1))
    }

    @Test
    fun stop_refreshing_by_intention() {
        rule.launchActivity(null)

        Screen.swipeRefresh.swipeRefreshElement.actions.pullToRefresh()

        rule.activity.postAndStopRefreshing()

        Screen.swipeRefresh.swipeRefreshElement.checks.isNotRefreshing()
    }
}
