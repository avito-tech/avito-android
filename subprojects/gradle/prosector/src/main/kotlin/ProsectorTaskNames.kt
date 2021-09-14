import com.avito.capitalize
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

internal fun prosectorTaskName(variantName: String): String = "prosectorUpload${variantName.capitalize()}"

public fun TaskContainer.prosectorTaskProvider(variantName: String): TaskProvider<ProsectorReleaseAnalysisTask> =
    typedNamed(prosectorTaskName(variantName))
