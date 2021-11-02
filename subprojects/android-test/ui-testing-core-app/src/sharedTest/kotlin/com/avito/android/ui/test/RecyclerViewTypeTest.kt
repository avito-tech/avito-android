package com.avito.android.ui.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.avito.android.test.annotations.DataSetNumber
import com.avito.android.test.app.core.screenRule
import com.avito.android.ui.StatefulRecyclerViewAdapterActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecyclerViewTypeTest {

    @get:Rule
    val rule = screenRule<StatefulRecyclerViewAdapterActivity>()

    fun test(dataSet: DataSet) {
        rule.launchActivity(null)
        Screen.statefulRecyclerViewAdapterScreen.list.checks
            .hasViewTypeAtPosition(dataSet.position, dataSet.correctViewType)

        Screen.statefulRecyclerViewAdapterScreen.list.checks
            .doesNotHaveViewTypeAtPosition(dataSet.position, dataSet.incrorrectViewType)
    }

    @DataSetNumber(1)
    @Test
    fun ds1() {
        test(DataSet(position = 0, correctViewType = 0, incrorrectViewType = 3))
    }

    @DataSetNumber(2)
    @Test
    fun ds2() {
        test(DataSet(position = 20, correctViewType = 20, incrorrectViewType = 19))
    }

    data class DataSet(val position: Int, val correctViewType: Int, val incrorrectViewType: Int)
}
