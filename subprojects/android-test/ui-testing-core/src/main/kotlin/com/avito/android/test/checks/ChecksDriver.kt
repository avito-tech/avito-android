package com.avito.android.test.checks

import androidx.test.espresso.ViewAssertion

interface ChecksDriver {

    fun check(assertion: ViewAssertion)
}
