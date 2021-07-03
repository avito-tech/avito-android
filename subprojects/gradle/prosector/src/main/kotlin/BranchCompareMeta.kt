internal data class BranchCompareMeta(
    val appPackage: String,
    val developer: String,
    val pullRequest: String,
    val beforeBuild: BuildInfo,
    val afterBuild: BuildInfo,
    val taskType: TaskType = TaskType.BRANCH_COMPARE
)
