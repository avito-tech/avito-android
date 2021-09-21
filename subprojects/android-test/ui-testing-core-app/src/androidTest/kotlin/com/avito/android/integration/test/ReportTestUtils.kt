package com.avito.android.integration.test

import com.google.common.truth.Truth

object ReportTestUtils {

    const val SPECIAL_TEST_CASE_ID = -1

    fun simpleSuccessAssertion() {
        val actual: Int = 2 + 2
        Truth.assertThat(actual).isEqualTo(4)
    }
}
