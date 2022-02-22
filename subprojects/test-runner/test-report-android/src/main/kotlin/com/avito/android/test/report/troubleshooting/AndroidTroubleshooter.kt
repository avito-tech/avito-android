package com.avito.android.test.report.troubleshooting

import com.avito.android.test.report.troubleshooting.dump.MainLooperQueueDumper
import com.avito.android.test.report.troubleshooting.dump.ViewHierarchyDumper

object TroubleshooterFactory {

    fun create(): Troubleshooter = Troubleshooter.Builder()
        .withDefaults()
        .add(MainLooperQueueDumper())
        .add(ViewHierarchyDumper())
        .build()
}
