package com.avito.android.runner.annotation.resolver

import com.avito.android.test.annotations.E2EStub
import com.avito.android.test.annotations.E2ETest
import com.avito.android.test.annotations.IntegrationTest
import com.avito.android.test.annotations.ManualTest
import com.avito.android.test.annotations.ScreenshotTest
import com.avito.android.test.annotations.SyntheticMonitoringTest
import com.avito.android.test.annotations.UIComponentStub
import com.avito.android.test.annotations.UIComponentTest
import com.avito.android.test.annotations.UnitTest
import com.avito.report.model.Kind

internal object TestKindExtractor {

    fun extract(test: TestMethodOrClass): Kind {
        val testTypes = arrayOf(
            UIComponentTest::class.java,
            E2ETest::class.java,
            IntegrationTest::class.java,
            ManualTest::class.java,
            UIComponentStub::class.java,
            E2EStub::class.java,
            UnitTest::class.java,
            ScreenshotTest::class.java,
            SyntheticMonitoringTest::class.java
        )
        val testAnnotations = Annotations.getAnnotationsSubset(test.testClass, test.testMethod, subset = *testTypes)

        require(testAnnotations.size <= 1) {
            "Test $test has multiple types but must be only one: $testAnnotations"
        }

        return when (val testType = testAnnotations.first()) {
            is UIComponentTest, is ScreenshotTest -> Kind.UI_COMPONENT
            is E2ETest, is SyntheticMonitoringTest -> Kind.E2E
            is IntegrationTest -> Kind.INTEGRATION
            is ManualTest -> Kind.MANUAL
            is UIComponentStub -> Kind.UI_COMPONENT_STUB
            is E2EStub -> Kind.E2E_STUB
            is UnitTest -> Kind.UNIT
            else -> throw IllegalArgumentException("Unsupported test type $testType")
        }
    }
}
