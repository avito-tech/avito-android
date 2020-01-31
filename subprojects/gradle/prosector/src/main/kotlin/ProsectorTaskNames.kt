import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.tasks.TaskContainer

internal fun prosectorTaskName(variantName: String): String = "prosectorUpload${variantName.capitalize()}"

fun TaskContainer.prosectorTaskProvider(variantName: String) =
    typedNamed<ProsectorReleaseAnalysisTask>(prosectorTaskName(variantName))
