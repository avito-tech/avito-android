package com.avito.plugin

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.android.apkFileProvider
import com.avito.android.bundleFileProvider
import com.avito.android.bundleTaskProvider
import com.avito.android.withAndroidApp
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.hasTasks
import com.avito.utils.hasContent
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.execution.TaskExecutionGraph
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dslx.closureOf
import org.gradle.util.Path
import java.io.File

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

            appExtension.applicationVariants.all { variant: ApplicationVariant ->

                registerTask(
                    tasks = target.tasks,
                    variant = variant,
                    taskName = signApkTaskName(variant.name),
                    serviceUrl = signExtension.host.orEmpty(),
                    archiveProvider = variant.apkFileProvider(),
                    providingTask = variant.packageApplicationProvider,
                    signTokensMap = signExtension.apkSignTokens
                )

                registerTask(
                    tasks = target.tasks,
                    variant = variant,
                    taskName = signBundleTaskName(variant.name),
                    serviceUrl = signExtension.host.orEmpty(),
                    archiveProvider = variant.bundleFileProvider(),
                    providingTask = target.tasks.bundleTaskProvider(variant),
                    signTokensMap = signExtension.bundleSignTokens
                )

                registeredBuildTypes[variant.name] = variant.buildType.name
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
        variant: ApplicationVariant,
        taskName: String,
        serviceUrl: String,
        archiveProvider: Provider<File>,
        providingTask: TaskProvider<*>,
        signTokensMap: Map<String, String?>
    ) {
        val buildTypeName = variant.buildType.name
        val token: String? = signTokensMap[buildTypeName]

        val isSignNeeded: Boolean = token.hasContent()

        // сигнал для agp что мы будем подписывать самостоятельно
        // todo не понятно как оно себя ведет с bundle
        variant.outputsAreSigned = isSignNeeded

        tasks.register<SignTask>(taskName) {
            group = taskGroup
            description = "Sign ${variant.name} with in-house service"

            val archiveFile = archiveProvider.get()
            // перезаписываем тот же файл
            unsignedFileProperty.set(archiveFile)
            signedFileProperty.set(archiveFile)

            this.serviceUrl.set(serviceUrl)
            tokenProperty.set(token)

            onlyIf { isSignNeeded }
        }.also {
            it.dependsOn(providingTask)
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
