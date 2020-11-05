package com.avito.android.build_verdict

internal class CompositeBuildVerdictWriter(
    private val writers: List<BuildVerdictWriter>
) : BuildVerdictWriter {
    override fun write(buildVerdict: BuildVerdict) {
        writers.forEach { writer -> writer.write(buildVerdict) }
    }
}
