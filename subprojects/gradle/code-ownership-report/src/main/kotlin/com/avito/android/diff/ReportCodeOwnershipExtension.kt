package com.avito.android.diff

import com.avito.android.diff.comparator.OwnersComparator
import com.avito.android.diff.extractor.OwnersExtractor
import com.avito.android.diff.formatter.OwnersDiffMessageFormatter
import com.avito.android.diff.report.OwnersDiffReportDestination
import org.gradle.api.provider.Property

public abstract class ReportCodeOwnershipExtension {

    public abstract val expectedOwnersExtractor: Property<OwnersExtractor>

    public abstract val actualOwnersExtractor: Property<OwnersExtractor>

    public abstract val diffReportDestination: Property<OwnersDiffReportDestination>

    public abstract val comparator: Property<OwnersComparator>

    public abstract val messageFormatter: Property<OwnersDiffMessageFormatter>
}
