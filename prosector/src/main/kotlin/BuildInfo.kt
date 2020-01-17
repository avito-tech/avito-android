import com.google.gson.annotations.SerializedName

data class BuildInfo(
    @SerializedName("id") val versionName: String,
    val buildType: String,
    val branchName: String,
    val commit: String
)