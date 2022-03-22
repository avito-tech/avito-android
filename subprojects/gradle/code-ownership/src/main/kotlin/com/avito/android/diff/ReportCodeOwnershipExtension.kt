package com.avito.android.diff

import com.avito.android.diff.comparator.EqualsOwnersComparator
import com.avito.android.diff.comparator.OwnersComparator
import com.avito.android.diff.formatter.DefaultOwnersDiffMessageFormatter
import com.avito.android.diff.formatter.OwnersDiffMessageFormatter
import com.avito.android.diff.provider.OwnersProvider
import com.avito.android.diff.report.OwnersDiffReportDestination
import org.gradle.api.provider.Property

public abstract class ReportCodeOwnershipExtension {

    public abstract val expectedOwnersProvider: Property<OwnersProvider>

    public abstract val actualOwnersProvider: Property<OwnersProvider>

    public abstract val diffReportDestination: Property<OwnersDiffReportDestination>

    public abstract val comparator: Property<OwnersComparator>

    public abstract val messageFormatter: Property<OwnersDiffMessageFormatter>

    init {
        comparator.convention(EqualsOwnersComparator())
        messageFormatter.convention(DefaultOwnersDiffMessageFormatter())
    }
}
