package com.avito.android.runner.annotation.validation

import com.avito.android.runner.annotation.resolver.TestMethodOrClass

interface TestMetadataValidator {

    fun validate(test: TestMethodOrClass)
}
