package com.avito.ci

import com.avito.ci.steps.ArtifactsConfiguration
import com.avito.ci.steps.BuildStep
import com.avito.ci.steps.CompileUiTests
import com.avito.ci.steps.ConfigurationCheck
import com.avito.ci.steps.DeployStep
import com.avito.ci.steps.ImpactAnalysisAwareBuildStep
import com.avito.ci.steps.LintCheck
import com.avito.ci.steps.MarkReportAsSourceForTMSStep
import com.avito.ci.steps.UiTestCheck
import com.avito.ci.steps.UnitTestCheck
import com.avito.ci.steps.UploadBuildResult
import com.avito.ci.steps.UploadToArtifactory
import com.avito.ci.steps.UploadToProsector
import com.avito.ci.steps.UploadToQapps
import com.avito.ci.steps.VerifyArtifactsStep
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class BuildStepListExtension(
    internal val buildStepListName: String,
    objects: ObjectFactory
) : Named {

    private val artifactsConfig = ArtifactsConfiguration()

    override fun getName() = buildStepListName

    internal val steps = objects.polymorphicDomainObjectContainer(BuildStep::class.java).apply {
        registerFactory(ConfigurationCheck::class.java) { name ->
            ConfigurationCheck(buildStepListName, name)
        }
        registerFactory(UiTestCheck::class.java) { name ->
            UiTestCheck(buildStepListName, name)
        }
        registerFactory(CompileUiTests::class.java) { name ->
            CompileUiTests(buildStepListName, name)
        }
        registerFactory(UnitTestCheck::class.java) { name ->
            UnitTestCheck(buildStepListName, name)
        }
        registerFactory(LintCheck::class.java) { name ->
            LintCheck(buildStepListName, name)
        }
        registerFactory(MarkReportAsSourceForTMSStep::class.java) { name ->
            MarkReportAsSourceForTMSStep(buildStepListName, name)
        }
        registerFactory(UploadToQapps::class.java) { name ->
            UploadToQapps(buildStepListName, artifactsConfig, name)
        }
        registerFactory(UploadToArtifactory::class.java) { name ->
            UploadToArtifactory(buildStepListName, artifactsConfig, name)
        }
        registerFactory(UploadToProsector::class.java) { name ->
            UploadToProsector(buildStepListName, artifactsConfig, name)
        }
        registerFactory(UploadBuildResult::class.java) { name ->
            UploadBuildResult(buildStepListName, name)
        }
        registerFactory(DeployStep::class.java) { name ->
            DeployStep(buildStepListName, artifactsConfig, name)
        }
        registerFactory(VerifyArtifactsStep::class.java) { name ->
            VerifyArtifactsStep(buildStepListName, artifactsConfig, name)
        }
    }

    //todo property
    var useImpactAnalysis: Boolean = true

    val taskDescription = objects.property<String>()

    fun configuration(closure: Closure<ConfigurationCheck>) {
        configureAndAdd("configuration", closure)
    }

    fun configuration(action: Action<ConfigurationCheck>) {
        configureAndAdd("configuration", action)
    }

    fun uiTests(closure: Closure<UiTestCheck>) {
        configureAndAdd("uiTests", closure)
    }

    fun uiTests(action: Action<UiTestCheck>) {
        configureAndAdd("uiTests", action)
    }

    fun compileUiTests(closure: Closure<CompileUiTests>) {
        configureAndAdd("compileUiTests", closure)
    }

    fun compileUiTests(action: Action<CompileUiTests>) {
        configureAndAdd("compileUiTests", action)
    }

    fun unitTests(closure: Closure<UnitTestCheck>) {
        configureAndAdd("unitTests", closure)
    }

    fun unitTests(action: Action<UnitTestCheck>) {
        configureAndAdd("unitTests", action)
    }

    fun lint(closure: Closure<LintCheck>) {
        configureAndAdd("lint", closure)
    }

    fun lint(action: Action<LintCheck>) {
        configureAndAdd("lint", action)
    }

    fun markReportAsSourceForTMS(closure: Closure<MarkReportAsSourceForTMSStep>) {
        configureAndAdd("markReportAsSourceForTMS", closure)
    }

    fun markReportAsSourceForTMS(action: Action<MarkReportAsSourceForTMSStep>) {
        configureAndAdd("markReportAsSourceForTMS", action)
    }

    fun uploadToQapps(closure: Closure<UploadToQapps>) {
        configureAndAdd("uploadToQapps", closure)
    }

    fun uploadToQapps(action: Action<UploadToQapps>) {
        configureAndAdd("uploadToQapps", action)
    }

    fun uploadToArtifactory(closure: Closure<UploadToArtifactory>) {
        configureAndAdd("uploadToArtifactory", closure)
    }

    fun uploadToArtifactory(action: Action<UploadToArtifactory>) {
        configureAndAdd("uploadToArtifactory", action)
    }

    fun uploadToProsector(closure: Closure<UploadToProsector>) {
        configureAndAdd("uploadToProsector", closure)
    }

    fun uploadToProsector(action: Action<UploadToProsector>) {
        configureAndAdd("uploadToProsector", action)
    }

    fun uploadBuildResult(closure: Closure<UploadBuildResult>) {
        configureAndAdd("uploadBuildResult", closure)
    }

    fun uploadBuildResult(action: Action<UploadBuildResult>) {
        configureAndAdd("uploadBuildResult", action)
    }

    fun deploy(closure: Closure<DeployStep>) {
        configureAndAdd("deploy", closure)
    }

    fun deploy(action: Action<DeployStep>) {
        configureAndAdd("deploy", action)
    }

    fun artifacts(closure: Closure<ArtifactsConfiguration>) {
        artifacts(Action {
            closure.delegate = artifactsConfig
            closure.resolveStrategy = Closure.DELEGATE_FIRST
            closure.call()
        })
    }

    fun artifacts(action: Action<ArtifactsConfiguration>) {
        action.execute(artifactsConfig)
        val step = steps.maybeCreate("artifacts", VerifyArtifactsStep::class.java)
        step.useImpactAnalysis = this.useImpactAnalysis
    }

    private inline fun <reified T : BuildStep> configureAndAdd(
        name: String,
        configure: Closure<T>
    ) {
        configureAndAdd(name, Action<T> { step ->
            configure.delegate = step
            configure.resolveStrategy = Closure.DELEGATE_FIRST
            configure.call()
        })
    }

    private inline fun <reified T : BuildStep> configureAndAdd(
        name: String,
        action: Action<T>
    ) {
        val step = steps.maybeCreate(name, T::class.java)
        action.execute(step)
        if (step is ImpactAnalysisAwareBuildStep) {
            step.useImpactAnalysis = this.useImpactAnalysis
        }
    }
}
