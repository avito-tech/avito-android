package com.avito.android.runner.report

import com.avito.android.runner.report.factory.LegacyReportFactory
import java.io.Serializable

public class StrategyFactory(
    private val factories: Map<String, LegacyReportFactory>
) : LegacyReportFactory, Serializable {

    override fun createReport(config: LegacyReportFactory.Config): Report =
        getFactory(config).createReport(config)

    override fun createLegacyReport(config: LegacyReportFactory.Config): LegacyReport =
        getFactory(config).createLegacyReport(config)

    override fun createReadReport(config: LegacyReportFactory.Config): ReadReport =
        getFactory(config).createReadReport(config)

    private fun getFactory(config: LegacyReportFactory.Config): LegacyReportFactory =
        requireNotNull(factories[config::class.java.simpleName]) {
            "Factory for config: $config hasn't found. You must register"
        }
}
