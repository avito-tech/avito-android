package com.avito.android.runner.annotation.validation

import com.avito.android.runner.annotation.resolver.TestMethodOrClass

class CompositeTestMetadataValidator(
    private val validators: List<TestMetadataValidator>
) : TestMetadataValidator {

    override fun validate(test: TestMethodOrClass) {
        validators.forEach { it.validate(test) }
    }
}
