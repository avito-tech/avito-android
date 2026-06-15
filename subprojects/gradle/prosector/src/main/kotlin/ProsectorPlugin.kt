
import com.avito.android.withAndroidApp
import com.avito.git.GitInfoBuildService
import com.avito.git.GitStateResult
import com.avito.git.gitInfoService
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public class ProsectorPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val config = target.extensions.create<ProsectorConfig>("prosector")
        val gitInfo = target.gitInfoService()

        target.withAndroidApp {

            it.applicationVariants
                .all { variant: @Suppress("DEPRECATION") com.android.build.gradle.api.ApplicationVariant ->

                    val packageTask = variant.packageApplicationProvider

                    target.tasks.register<ProsectorReleaseAnalysisTask>(prosectorTaskName(variant.name)) {
                        group = "ci"
                        debug = config.debug
                        host = config.host

                        // Hoist serializable primitives BEFORE building the lazy provider: the map{}
                        // lambda below is serialized into this task's `meta` Property for the
                        // configuration cache, so it must close over Strings + the injected `service`
                        // only — never the AGP variant or the prosector extension, which are not
                        // configuration-cache-serializable. Branch/commit are always resolved from git
                        // lazily at task-execution time via `service`, so applying the plugin never
                        // reads git at configuration time and a commit / branch switch doesn't
                        // invalidate the configuration cache. (ProsectorConfig.branchName/commitHash
                        // are deprecated and ignored.) See MBSA-2360.
                        val appPackage = variant.applicationId
                        val versionName = variant.versionName
                        val buildType = variant.name

                        meta.set(
                            gitInfo.map { service ->
                                ReleaseAnalysisMeta(
                                    appPackage = appPackage,
                                    buildInfo = BuildInfo(
                                        versionName = versionName,
                                        buildType = buildType,
                                        branchName = service.gitBranchOrLocal(),
                                        commit = service.gitCommitOrLocal()
                                    )
                                )
                            }
                        )
                    dependsOn(packageTask)
                }
            }
        }
    }
}

private fun GitInfoBuildService.gitBranchOrLocal(): String =
    when (val result = getGitStateResult()) {
        is GitStateResult.Available -> result.state.currentBranch.name
        is GitStateResult.Unavailable -> "local"
    }

private fun GitInfoBuildService.gitCommitOrLocal(): String =
    when (val result = getGitStateResult()) {
        is GitStateResult.Available -> result.state.currentBranch.commit
        is GitStateResult.Unavailable -> "local"
    }
