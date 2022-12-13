package com.avito.android.tech_budget.ab_tests

import org.gradle.api.provider.Property

public abstract class CollectABTestsConfiguration {

    /**
     * Name of task used to collect all project AB tests.
     *
     * Task should be inherited from [CollectProjectABTestsTask] and declare an output file with AB tests.
     */
    public abstract val collectProjectABTestsTaskName: Property<String>

    /**
     * Used to deserialize A/B tests output from [CollectProjectABTestsTask].
     *
     * Output can be collected in any format, but then must be transformed to a [ABTest] model with this parser.
     *
     * Default implementation uses JSON in format:
     * ```json
     * [
     *  {
     *      "key": "string",
     *      "defaultGroup": "string",
     *      "groups": [ "string" ],
     *      "owners": [ "string"]
     *  }
     * ]
     * ```
     */
    public abstract val abTestsFileParser: Property<ABTestsFileParser>
}
