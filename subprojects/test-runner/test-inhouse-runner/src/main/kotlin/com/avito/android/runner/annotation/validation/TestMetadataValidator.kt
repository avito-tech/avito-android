package com.avito.android.runner.annotation.validation

import com.avito.android.runner.annotation.resolver.TestMethodOrClass

public interface TestMetadataValidator {

    public fun validate(test: TestMethodOrClass)
}
