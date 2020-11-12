package com.avito.plugin

import com.android.build.gradle.api.ApplicationVariant
import com.avito.android.androidCommonExtension
import com.avito.android.withAndroidApp
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.hasTasks
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.execution.TaskExecutionGraph
import com.android.build.api.variant.Variant
import com.android.build.api.artifact.ArtifactType
import org.gradle.kotlin.dsl.register
import com.avito.android.taskName
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dslx.closureOf
import org.gradle.util.Path
import java.util.Objects.requireNonNull
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Подписываем apk при помощи собственного сервиса.
 *
 * Пример использования:
 *
 * ```
 * применяем к модулю приложения (порядок не важен)
 * plugins {
 *   id("com.android.application")
 *   id("com.avito.android.signer")
 * }
 *
 * регистрируем какие buildVariant'ы мы хотим подписывать при помощи сервиса
 * неуказанные варианты будут использовать стандартный механизм подписи android gradle plugin
 * https://developer.android.com/studio/publish/app-signing
 *
 * signService {
 *
 *          buildType                         String токен сервиса             sha1 от подписи для проверки перед отправкой
 *
 *   apk(android.buildTypes.release, project.properties.get("avitoSignToken"), "<sha1 checksum>")
 *   bundle(android.buildTypes.release, project.properties.get("avitoSignBundleToken"), "<sha1 checksum>")
 * }
 * ```
 * [SignExtension]
 *
 * Плагин генерирует каждому варианту следущие таски:
 * - signApkViaService<Variant>
 * - signBundleViaService<Variant>
 *
 * Если какой-то таске требуется получить подписанную apk/bundle, следует указать dependsOn(signXXX)

 * Локально таски молча скипнутся если не предоставить нужный для варианта token, на CI есть механизм защиты от этого, [failOnMissingToken]
 *
 * Плагин соблюдает неявный контракт: apk и aab файлы заменяются после подписи по тому же пути где лежали неподписанными
 *
 * Есть возможность отключить плагин флагом билда: `disableSignService` = true
 */
@Suppress("UnstableApiUsage")
class SignServicePlugin : Plugin<Project> {

    private val taskGroup = "ci"

    override fun apply(target: Project) {
        val signExtension = target.extensions.create<SignExtension>("signService")

        //todo rename to `avito.signer.disable`
        if (target.getBooleanProperty("disableSignService")) {
            return
        }

        //todo explain why do we have multiple options to skip signing
        // disableSignService (avito.signer.disable) + avito.signer.allowSkip
        // Is it feasible to have only one?
        val skipSigning: Boolean = target.getBooleanProperty("avito.signer.allowSkip")

        target.afterEvaluate {
            if (!skipSigning) {
                require(!signExtension.host.isNullOrBlank()) { "signService.host must be set" }
            }
        }

        val registeredBuildTypes = mutableMapOf<String, String>()

        target.withAndroidApp { appExtension ->

            target.androidCommonExtension.onVariants {
                val variant = this

                registerTask(
                    tasks = target.tasks,
                    variant = this,
                    taskName = signApkTaskName(variant),
                    serviceUrl = signExtension.host.orEmpty(),
                    signTokensMap = signExtension.apkSignTokens
                )

                registerTask(
                    tasks = target.tasks,
                    variant = this,
                    taskName = signBundleTaskName(variant),
                    serviceUrl = signExtension.host.orEmpty(),
                    signTokensMap = signExtension.bundleSignTokens
                )

                registeredBuildTypes[variant.name] = requireNotNull(variant.buildType)
            }

            target.androidCommonExtension.onVariantProperties {

                artifacts.use(target.tasks.signedApkTaskProvider(this))
                    .wiredWithDirectories(
                        taskInput = SignTask::unsignedDirProperty,
                        taskOutput = SignTask::signedDirProperty
                    )
                    .toTransform(ArtifactType.APK)

                artifacts.use(target.tasks.signedBundleTaskProvider(this))
                    .wiredWithFiles(
                        taskInput = SignTask::unsignedFileProperty,
                        taskOutput = SignTask::signedFileProperty
                    )
                    .toTransform(ArtifactType.BUNDLE)
            }

            appExtension.applicationVariants.all { variant: ApplicationVariant ->

                val buildTypeName = variant.buildType.name
                val apkToken: String? = signExtension.apkSignTokens[buildTypeName]
                val bundleToken: String? = signExtension.bundleSignTokens[buildTypeName]

                variant.outputsAreSigned = apkToken.hasContent() || bundleToken.hasContent()
            }
        }

        if (!skipSigning) {
            target.gradle.taskGraph.whenReady(closureOf<TaskExecutionGraph> {
                failOnMissingToken(
                    projectPath = target.path,
                    variantToBuildType = registeredBuildTypes,
                    taskExecutionGraph = this,
                    apkSignTokens = signExtension.apkSignTokens,
                    bundleSignTokens = signExtension.bundleSignTokens
                )
            })
        }
    }

    private fun registerTask(
        tasks: TaskContainer,
        variant: Variant<*>,
        taskName: String,
        serviceUrl: String,
        signTokensMap: Map<String, String?>
    ) {
        val buildTypeName = requireNonNull(variant.buildType)
        val token: String? = signTokensMap[buildTypeName]

        val isSignNeeded: Boolean = token.hasContent()

        tasks.register<SignTask>(taskName) {
            group = taskGroup
            description = "Sign ${variant.name} with in-house service"

            this.serviceUrl.set(serviceUrl)
            tokenProperty.set(token)

            onlyIf { isSignNeeded }
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
            requireNotNull(signTokens[buildTypeName]) { "[SignServicePlugin] can't sign $buildTypeName, token is not set" }
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
