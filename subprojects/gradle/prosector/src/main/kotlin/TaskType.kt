import com.google.gson.annotations.SerializedName

public enum class TaskType {

    /**
     * сравнение ветки с develop (например. технически - сравнение любой ветки с любой),
     * требуется информация о beforeBuild и afterBuild, а также обе сборки.
     */
    @SerializedName("branch-compare")
    BRANCH_COMPARE,

    /**
     * анализ релизной сборки, требуется информация о ней в afterBuild и сама сборка в build_after
     */
    @SerializedName("release-analysis")
    RELEASE_ANALYSIS
}
