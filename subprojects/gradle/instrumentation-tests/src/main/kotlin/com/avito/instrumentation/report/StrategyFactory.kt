package com.avito.instrumentation.report

import java.io.Serializable

public class StrategyFactory(
    private val factories: Map<String, ReportFactory>
) : ReportFactory, Serializable {

    override fun createReport(config: ReportFactory.Config): Report =
        getFactory(config).createReport(config)

    override fun createReadReport(config: ReportFactory.Config): ReadReport =
        getFactory(config).createReadReport(config)

    private fun getFactory(config: ReportFactory.Config): ReportFactory =
        requireNotNull(factories[config::class.java.simpleName]) {
            "Factory for config: $config hasn't found. You must register"
        }
}
