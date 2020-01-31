import com.google.gson.annotations.SerializedName

data class ReleaseAnalysisMeta(
    val appPackage: String,
    @SerializedName("afterBuild") val buildInfo: BuildInfo,
    val taskType: TaskType = TaskType.RELEASE_ANALYSIS
)