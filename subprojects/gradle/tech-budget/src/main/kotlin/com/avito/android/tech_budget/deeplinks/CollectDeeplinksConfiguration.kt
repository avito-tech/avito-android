package com.avito.android.tech_budget.deeplinks

import org.gradle.api.provider.Property

public abstract class CollectDeeplinksConfiguration {

    /**
     * Name of task used to collect all project deepLinks.
     *
     * Task should be inherited from [CollectProjectDeeplinksTask] and declare an output file with deeplinks.
     */
    public abstract val collectProjectDeeplinksTaskName: Property<String>

    /**
     * Used to deserialize deepLinks output from [CollectProjectDeeplinksTask].
     *
     * Output can be collected in any format, but then must be transformed to a [DeepLink] model with this parser.
     *
     * Default implementation uses JSON in format:
     * ```json
     * [
     *  {
     *      "deepLinkName": "ExampleLink",
     *      "moduleName": ":example-module",
     *      "path": "/example",
     *      "version": 1,
     *      "owners": ["ExampleOwner"]
     *  }
     * ]
     * ```
     */
    public abstract val deepLinksFileParser: Property<DeepLinksFileParser>
}
