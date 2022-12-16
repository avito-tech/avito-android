package com.avito.android.tech_budget.feature_toggles

import com.avito.android.tech_budget.parser.FileParser
import org.gradle.api.provider.Property

public abstract class CollectFeatureTogglesConfiguration {

    /**
     * Name of task used to collect all project feature toggles.
     *
     * Task should be inherited from [CollectProjectFeatureTogglesTask] and declare an output file with feature toggles.
     */
    public abstract val collectProjectFeatureTogglesTaskName: Property<String>

    /**
     * Used to deserialize feature toggle output from [CollectProjectFeatureTogglesTask].
     *
     * Output can be collected in any format, but then must be transformed to a [FeatureToggle] model with this parser.
     *
     * Default implementation uses JSON in format:
     * ```json
     * [
     *  {
     *      "key": "string",
     *      "defaultValue": "string",
     *      "description": "string",
     *      "isRemote": true,
     *      "owners": [ "string"]
     *  }
     * ]
     * ```
     */
    public abstract val featureTogglesFileParser: Property<FileParser<FeatureToggle>>
}
