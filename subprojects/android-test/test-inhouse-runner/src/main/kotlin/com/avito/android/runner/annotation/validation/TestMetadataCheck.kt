package com.avito.android.runner.annotation.validation

import com.avito.android.runner.annotation.resolver.TestMethodOrClass

interface TestMetadataCheck {

    fun validate(test: TestMethodOrClass)
}
