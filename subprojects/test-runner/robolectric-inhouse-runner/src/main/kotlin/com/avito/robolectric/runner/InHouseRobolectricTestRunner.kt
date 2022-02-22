package com.avito.robolectric.runner

import com.avito.robolectric.runner.description.RobolectricDescriptionMetaDataParser
import com.avito.test.report.listener.RobolectricReportTestListener
import org.junit.runner.notification.RunNotifier
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.inject.Injector

public open class InHouseRobolectricTestRunner
@JvmOverloads constructor(
    private val testClass: Class<*>,
    injector: Injector? = defaultInjector().build()
) : RobolectricTestRunner(testClass, injector) {

    override fun run(notifier: RunNotifier) {
        notifier.addListener(
            RobolectricReportTestListener(
                testClass = testClass,
                descriptionMetadataParser = RobolectricDescriptionMetaDataParser()
            )
        )
        super.run(notifier)
    }
}
