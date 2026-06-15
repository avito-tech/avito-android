public open class ProsectorConfig {

    public lateinit var host: String

    @Deprecated(
        "Ignored. Branch and commit are always resolved from git via GitInfoBuildService at " +
            "task-execution time (configuration-cache-safe, no config-time git read). Kept only for " +
            "source compatibility; assigning a value has no effect. See MBSA-2360.",
        level = DeprecationLevel.WARNING,
    )
    public var branchName: String = ""

    @Deprecated(
        "Ignored. Branch and commit are always resolved from git via GitInfoBuildService at " +
            "task-execution time (configuration-cache-safe, no config-time git read). Kept only for " +
            "source compatibility; assigning a value has no effect. See MBSA-2360.",
        level = DeprecationLevel.WARNING,
    )
    public var commitHash: String = ""

    public var debug: Boolean = false
}
