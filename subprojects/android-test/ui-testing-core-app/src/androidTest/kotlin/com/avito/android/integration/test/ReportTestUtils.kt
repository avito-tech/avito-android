package com.avito.android.integration.test

import com.avito.android.util.Is
import org.hamcrest.MatcherAssert.assertThat

object ReportTestUtils {

    const val SPECIAL_TEST_CASE_ID = -1

    fun simpleSuccessAssertion() = assertThat(2 + 2, Is(4))
}
