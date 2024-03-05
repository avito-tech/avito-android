package com.avito.android.test.checks

import androidx.test.espresso.ViewAssertion

public interface ChecksDriver {

    public fun check(assertion: ViewAssertion)
}
