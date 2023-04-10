package com.avito.android.plugin.build_metrics.internal.gradle.tasks.compile

internal class KspJvmMetric(
    override val time: Long,
    override val moduleName: String,
    override val taskName: String
) : BaseCompileMetric() {
    override val taskType: String = "KspJvm"
}
