
import com.avito.android.withAndroidApp
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public class ProsectorPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val config = target.extensions.create<ProsectorConfig>("prosector")

        target.withAndroidApp {

            it.applicationVariants
                .all { variant: @Suppress("DEPRECATION") com.android.build.gradle.api.ApplicationVariant ->

                    val packageTask = variant.packageApplicationProvider

                    target.tasks.register<ProsectorReleaseAnalysisTask>(prosectorTaskName(variant.name)) {
                        group = "ci"
                        debug = config.debug
                        host = config.host
                        meta = ReleaseAnalysisMeta(
                            appPackage = variant.applicationId,
                            buildInfo = BuildInfo(
                                versionName = variant.versionName,
                                buildType = variant.name,
                                branchName = config.branchName,
                                commit = config.commitHash
                            )
                        )
                    dependsOn(packageTask)
                }
            }
        }
    }
}
