package com.avito.plugin

import com.android.build.api.artifact.ArtifactType
import com.android.build.api.variant.Variant
import com.android.build.gradle.api.ApplicationVariant
import com.avito.android.androidCommonExtension
import com.avito.android.bundleTaskProvider
import com.avito.android.withAndroidApp
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.hasTasks
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dslx.closureOf
import org.gradle.util.Path
import java.util.Objects.requireNonNull
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Suppress("UnstableApiUsage")
class SignServicePlugin : Plugin<Project> {

    private val taskGroup = "ci"

    override fun apply(target: Project) {
        val signExtension = target.extensions.create<SignExtension>("signService")

        // todo rename to `avito.signer.disable`
        if (target.getBooleanProperty("disableSignService")) {
            return
        }

        // todo explain why do we have multiple options to skip signing
        //  disableSignService (avito.signer.disable) + avito.signer.allowSkip
        //  Is it feasible to have only one?
        val skipSigning: Boolean = target.getBooleanProperty("avito.signer.allowSkip")

        val registeredBuildTypes = mutableMapOf<String, String>()

        target.withAndroidApp { appExtension ->

            target.androidCommonExtension.onVariants {
                val variant = this

                registerTask(
                    tasks = target.tasks,
                    type = SignApkTask::class.java,
                    variant = this,
                    taskName = signApkTaskName(variant),
                    signTokensMap = signExtension.apkSignTokens,
                    extension = signExtension,
                )

                registerTask(
                    tasks = target.tasks,
                    type = SignBundleTask::class.java,
                    variant = this,
                    taskName = signBundleTaskName(variant),
                    signTokensMap = signExtension.bundleSignTokens,
                    extension = signExtension,
                )

                registeredBuildTypes[variant.name] = requireNotNull(variant.buildType)
            }

            target.androidCommonExtension.onVariantProperties {

                artifacts.use(target.tasks.signedApkTaskProvider(this))
                    .wiredWithDirectories(
                        taskInput = SignApkTask::unsignedDirProperty,
                        taskOutput = SignApkTask::signedDirProperty
                    )
                    .toTransform(ArtifactType.APK)

                artifacts.use(target.tasks.signedBundleTaskProvider(this))
                    .wiredWithFiles(
                        taskInput = SignBundleTask::unsignedFileProperty,
                        taskOutput = SignBundleTask::signedFileProperty
                    )
                    .toTransform(ArtifactType.BUNDLE)
            }

            appExtension.applicationVariants.all { variant: ApplicationVariant ->

                val buildTypeName = variant.buildType.name
                val apkToken: String? = signExtension.apkSignTokens[buildTypeName]
                val bundleToken: String? = signExtension.bundleSignTokens[buildTypeName]

                variant.outputsAreSigned = apkToken.hasContent() || bundleToken.hasContent()

                target.tasks.signedApkTaskProvider(variant.name).configure {
                    it.dependsOn(variant.packageApplicationProvider)
                }

                target.tasks.signedBundleTaskProvider(variant.name).configure {
                    it.dependsOn(target.tasks.bundleTaskProvider(variant))
                }
            }
        }

        if (!skipSigning) {
            target.gradle.taskGraph.whenReady(
                closureOf<TaskExecutionGraph> {
                    failOnMissingToken(
                        projectPath = target.path,
                        variantToBuildType = registeredBuildTypes,
                        taskExecutionGraph = this,
                        apkSignTokens = signExtension.apkSignTokens,
                        bundleSignTokens = signExtension.bundleSignTokens
                    )
                }
            )
        }
    }

    // TODO: extract to factory
    private fun registerTask(
        tasks: TaskContainer,
        type: Class<out SignArtifactTask>,
        variant: Variant<*>,
        taskName: String,
        signTokensMap: Map<String, String?>,
        extension: SignExtension,
    ) {
        val buildTypeName = requireNonNull(variant.buildType)
        val token: String? = signTokensMap[buildTypeName]

        val isSignNeeded: Boolean = token.hasContent()

        tasks.register(taskName, type) {
            it.group = taskGroup
            it.description = "Sign ${variant.name} with in-house service"

            it.serviceUrl.set(resolveServiceUrl(extension))
            it.tokenProperty.set(token)

            it.readWriteTimeoutSec.set(extension.readWriteTimeoutSec.convention(DEFAULT_TIMEOUT_SEC))

            it.onlyIf { isSignNeeded }
        }
    }

    @Suppress("DEPRECATION")
    private fun resolveServiceUrl(extension: SignExtension): String {
        val value = extension.url
            .orElse(extension.host.orEmpty())
            .get()

        return validateUrl(value)
    }

    private fun validateUrl(url: String): String {
        return try {
            url.toHttpUrl()
            url
        } catch (e: Throwable) {
            throw IllegalArgumentException("Invalid signer url value: '$url'", e)
        }
    }

    private fun failOnMissingToken(
        projectPath: String,
        variantToBuildType: Map<String, String>,
        taskExecutionGraph: TaskExecutionGraph,
        apkSignTokens: Map<String, String?>,
        bundleSignTokens: Map<String, String?>
    ) {
        variantToBuildType.forEach { (variantName, buildTypeName) ->
            checkToken(
                buildTypeName = buildTypeName,
                signTokens = apkSignTokens,
                signTaskPath = Path.path("$projectPath:${signApkTaskName(variantName)}"),
                taskExecutionGraph = taskExecutionGraph
            )
            checkToken(
                buildTypeName = buildTypeName,
                signTokens = bundleSignTokens,
                signTaskPath = Path.path("$projectPath:${signBundleTaskName(variantName)}"),
                taskExecutionGraph = taskExecutionGraph
            )
        }
    }

    private fun checkToken(
        buildTypeName: String,
        signTokens: Map<String, String?>,
        signTaskPath: Path,
        taskExecutionGraph: TaskExecutionGraph
    ) {
        val isSignIntended = signTokens.containsKey(buildTypeName)

        if (isSignIntended && taskExecutionGraph.hasTasks(setOf(signTaskPath))) {
            requireNotNull(signTokens[buildTypeName]) {
                "[SignServicePlugin] can't sign $buildTypeName, token is not set"
            }
        }
    }
}

@OptIn(ExperimentalContracts::class)
private fun String?.hasContent(): Boolean {
    contract {
        returns(true) implies (this@hasContent != null)
    }

    if (isNullOrBlank()) return false
    if (this == "null") return false
    return true
}

private const val DEFAULT_TIMEOUT_SEC = 40L
