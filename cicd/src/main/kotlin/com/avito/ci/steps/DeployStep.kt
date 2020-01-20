package com.avito.ci.steps

import com.avito.android.plugin.artifactory.artifactoryAppBackupTask
import com.avito.cd.AndroidArtifactType.APK
import com.avito.cd.AndroidArtifactType.BUNDLE
import com.avito.cd.CdBuildConfig
import com.avito.cd.cdBuildConfig
import com.avito.cd.isCdBuildConfigPresent
import com.avito.upload_to_googleplay.GooglePlayDeploy
import com.avito.upload_to_googleplay.registerDeployToGooglePlayTask
import com.avito.utils.logging.ciLogger
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import java.io.File

class DeployStep(
    context: String,
    private val artifacts: ArtifactsConfiguration
) : SuppressibleBuildStep(context) {

    override fun registerTask(
        project: Project,
        rootTask: TaskProvider<out Task>
    ) {
        if (project.isCdBuildConfigPresent) {
            registerDeploymentsTasks(
                project.cdBuildConfig.get().deployments,
                project,
                rootTask
            )
        } else {
            project.ciLogger.info("Configure deploy step without cd build config")
        }
    }

    // todo есть сомнения в правильности этого места
    private fun registerDeploymentsTasks(
        deployments: List<CdBuildConfig.Deployment>,
        project: Project,
        rootTask: TaskProvider<out Task>
    ) {
        val googlePlayDeployments = mapDeploymentsToGooglePlayDeploys(deployments)
        if (googlePlayDeployments.isNotEmpty()) {
            val uploadToPlayMarketTask = project.tasks.registerDeployToGooglePlayTask(
                deploys = googlePlayDeployments
            ) {
                dependsOn(verifyTaskName(context))
                dependsOn(project.tasks.artifactoryAppBackupTask())
            }
            rootTask.configure { it.finalizedBy(uploadToPlayMarketTask) }
        }
    }

    private fun mapDeploymentsToGooglePlayDeploys(
        deployments: List<CdBuildConfig.Deployment>
    ): List<GooglePlayDeploy> {
        return deployments
            .asSequence()
            .filterIsInstance<CdBuildConfig.Deployment.GooglePlay>()
            .map { deployment ->
                try {
                    val outputs = artifacts.outputs.values
                    val mappings = outputs.findOutput<Output.ProguardMapping> { artifact ->
                        artifact.variant == deployment.buildVariant
                    }
                    when (deployment.artifactType) {
                        APK -> {
                            val apk = outputs.findOutput<Output.ApkOutput> { output ->
                                output.variant == deployment.buildVariant
                            }

                            GooglePlayDeploy(
                                binaryType = GooglePlayDeploy.BinaryType.APK,
                                track = deployment.track.name.toLowerCase(),
                                applicationId = apk.packageName,
                                binary = File(apk.path),
                                mapping = File(mappings.path)
                            )
                        }
                        BUNDLE -> {
                            val bundle = outputs.findOutput<Output.BundleOutput> { output ->
                                output.variant == deployment.buildVariant
                            }

                            GooglePlayDeploy(
                                binaryType = GooglePlayDeploy.BinaryType.BUNDLE,
                                track = deployment.track.name.toLowerCase(),
                                applicationId = bundle.packageName,
                                binary = File(bundle.path),
                                mapping = File(mappings.path)
                            )
                        }
                    }
                } catch (e: Exception) {
                    throw IllegalStateException("Can't configure deployment=$deployment", e)
                }
            }
            .toList()
    }

    private inline fun <reified O : Output> Iterable<Output>.findOutput(noinline predicate: (O) -> Boolean): O {
        val outputs: Sequence<O> = asSequence()
            .filterIsInstance<O>()
            .filter(predicate)

        outputs.ifEmpty { throw IllegalStateException("Cant' find output ${O::class.java} in ${this}") }

        return outputs.singleOrNull()
            ?: throw IllegalStateException("Too many outputs: $outputs of type ${O::class.java} in ${this}")
    }
}
