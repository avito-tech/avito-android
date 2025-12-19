package com.avito.android.gradle_configuration.extension.spec

import com.avito.android.gradle_configuration.extension.nested.ReportViewerExtension
import org.gradle.api.Action
import org.gradle.api.tasks.Nested

public abstract class BaseNupokatiPipelineSpec : NupokatiPipelineSpec {

    @get:Nested
    public abstract val reportViewer: ReportViewerExtension

    public fun reportViewer(action: Action<ReportViewerExtension>) {
        action.execute(reportViewer)
    }
}
