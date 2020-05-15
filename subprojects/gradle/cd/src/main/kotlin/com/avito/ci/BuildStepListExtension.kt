package com.avito.ci

import com.avito.ci.steps.ArtifactsConfiguration
import com.avito.ci.steps.BuildStep
import com.avito.ci.steps.CompileUiTests
import com.avito.ci.steps.ConfigurationCheck
import com.avito.ci.steps.DeployStep
import com.avito.ci.steps.ImpactAnalysisAwareBuildStep
import com.avito.ci.steps.LintCheck
import com.avito.ci.steps.PerformanceTestCheck
import com.avito.ci.steps.UiTestCheck
import com.avito.ci.steps.UnitTestCheck
import com.avito.ci.steps.UploadBuildResult
import com.avito.ci.steps.UploadToArtifactory
import com.avito.ci.steps.UploadToProsector
import com.avito.ci.steps.UploadToQapps
import com.avito.ci.steps.VerifyArtifactsStep
import groovy.lang.Closure
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.kotlin.dsl.property

@Suppress("UnstableApiUsage")
open class BuildStepListExtension(internal val name: String, objects: ObjectFactory) {

    private val artifactsConfig = ArtifactsConfiguration()

    internal val description = objects.property<String>()

    internal val steps: ListProperty<BuildStep> = objects.listProperty(BuildStep::class.java).empty()

    internal var useImpactAnalysis: Boolean = false

    fun configuration(closure: Closure<ConfigurationCheck>) {
        configureAndAdd(ConfigurationCheck(name), closure)
    }

    fun configuration(action: Action<ConfigurationCheck>) {
        configureAndAdd(ConfigurationCheck(name), action)
    }

    fun uiTests(closure: Closure<UiTestCheck>) {
        configureAndAdd(UiTestCheck(name), closure)
    }

    fun uiTests(action: Action<UiTestCheck>) {
        configureAndAdd(UiTestCheck(name), action)
    }

    fun performanceTests(closure: Closure<PerformanceTestCheck>) {
        configureAndAdd(PerformanceTestCheck(name), closure)
    }

    fun performanceTests(action: Action<PerformanceTestCheck>) {
        configureAndAdd(PerformanceTestCheck(name), action)
    }

    fun compileUiTests(closure: Closure<CompileUiTests>) {
        configureAndAdd(CompileUiTests(name), closure)
    }

    fun compileUiTests(action: Action<CompileUiTests>) {
        configureAndAdd(CompileUiTests(name), action)
    }

    fun unitTests(closure: Closure<UnitTestCheck>) {
        configureAndAdd(UnitTestCheck(name), closure)
    }

    fun unitTests(action: Action<UnitTestCheck>) {
        configureAndAdd(UnitTestCheck(name), action)
    }

    fun lint(closure: Closure<LintCheck>) {
        configureAndAdd(LintCheck(name), closure)
    }

    fun lint(action: Action<LintCheck>) {
        configureAndAdd(LintCheck(name), action)
    }

    fun uploadToQapps(closure: Closure<UploadToQapps>) {
        configureAndAdd(UploadToQapps(name, artifactsConfig), closure)
    }

    fun uploadToQapps(action: Action<UploadToQapps>) {
        configureAndAdd(UploadToQapps(name, artifactsConfig), action)
    }

    fun uploadToArtifactory(closure: Closure<UploadToArtifactory>) {
        configureAndAdd(UploadToArtifactory(name, artifactsConfig), closure)
    }

    fun uploadToArtifactory(action: Action<UploadToArtifactory>) {
        configureAndAdd(UploadToArtifactory(name, artifactsConfig), action)
    }

    fun uploadToProsector(closure: Closure<UploadToProsector>) {
        configureAndAdd(UploadToProsector(name, artifactsConfig), closure)
    }

    fun uploadToProsector(action: Action<UploadToProsector>) {
        configureAndAdd(UploadToProsector(name, artifactsConfig), action)
    }

    fun uploadBuildResult(closure: Closure<UploadBuildResult>) {
        configureAndAdd(UploadBuildResult(name), closure)
    }

    fun uploadBuildResult(action: Action<UploadBuildResult>) {
        configureAndAdd(UploadBuildResult(name), action)
    }

    fun deploy(closure: Closure<DeployStep>) {
        configureAndAdd(DeployStep(name, artifactsConfig), closure)
    }

    fun deploy(action: Action<DeployStep>) {
        configureAndAdd(DeployStep(name, artifactsConfig), action)
    }

    fun artifacts(closure: Closure<ArtifactsConfiguration>) {
        val step = VerifyArtifactsStep(name, artifactsConfig)
        steps.add(step)

        closure.delegate = artifactsConfig
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()

        step.useImpactAnalysis = this.useImpactAnalysis
    }

    fun artifacts(action: Action<ArtifactsConfiguration>) {
        val step = VerifyArtifactsStep(name, artifactsConfig)
        steps.add(step)

        action.execute(artifactsConfig)

        step.useImpactAnalysis = this.useImpactAnalysis
    }

    private fun <T : BuildStep> configureAndAdd(step: T, configure: Closure<T>) {
        configure.delegate = step
        configure.resolveStrategy = Closure.DELEGATE_FIRST
        configure.call()
        if (step is ImpactAnalysisAwareBuildStep) {
            step.useImpactAnalysis = this.useImpactAnalysis
        }
        steps.add(step)
    }

    private fun <T : BuildStep> configureAndAdd(step: T, action: Action<T>) {
        action.execute(step)
        if (step is ImpactAnalysisAwareBuildStep) {
            step.useImpactAnalysis = this.useImpactAnalysis
        }
        steps.add(step)
    }
}
