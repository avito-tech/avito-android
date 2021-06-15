package com.avito.ci

import com.avito.android.Problem
import com.avito.android.asRuntimeException
import com.avito.ci.steps.ArtifactsConfiguration
import com.avito.ci.steps.BuildStep
import com.avito.ci.steps.CompileUiTests
import com.avito.ci.steps.ConfigurationCheck
import com.avito.ci.steps.CustomTaskStep
import com.avito.ci.steps.DeployStep
import com.avito.ci.steps.DetektCheck
import com.avito.ci.steps.FlakyReportStep
import com.avito.ci.steps.ImpactAnalysisAwareBuildStep
import com.avito.ci.steps.ImpactMetrics
import com.avito.ci.steps.LintCheck
import com.avito.ci.steps.MarkReportAsSourceForTMSStep
import com.avito.ci.steps.TestSummaryStep
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
    private val explicitOverride: Boolean,
    objects: ObjectFactory
) : Named {

    private val artifactsConfig = ArtifactsConfiguration()

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
        registerFactory(ImpactMetrics::class.java) { name ->
            ImpactMetrics(buildStepListName, name)
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
        registerFactory(TestSummaryStep::class.java) { name ->
            TestSummaryStep(buildStepListName, name)
        }
        registerFactory(FlakyReportStep::class.java) { name ->
            FlakyReportStep(buildStepListName, name)
        }
        registerFactory(DetektCheck::class.java) { name ->
            DetektCheck(buildStepListName, name)
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
        registerFactory(CustomTaskStep::class.java) { name ->
            CustomTaskStep(buildStepListName, name)
        }
    }

    // todo property
    var useImpactAnalysis: Boolean = true

    val taskDescription = objects.property<String>()

    override fun getName() = buildStepListName

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

    fun impactMetrics(closure: Closure<ImpactMetrics>) {
        configureAndAdd("impactMetrics", closure)
    }

    fun impactMetrics(action: Action<ImpactMetrics>) {
        configureAndAdd("impactMetrics", action)
    }

    fun unitTests(closure: Closure<UnitTestCheck>) {
        configureAndAdd("unitTests", closure)
    }

    fun unitTests(action: Action<UnitTestCheck>) {
        configureAndAdd("unitTests", action)
    }

    fun detekt(closure: Closure<DetektCheck>) {
        configureAndAdd("detekt", closure)
    }

    fun detekt(action: Action<DetektCheck>) {
        configureAndAdd("detekt", action)
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

    fun testSummary(closure: Closure<TestSummaryStep>) {
        configureAndAdd("testSummary", closure)
    }

    fun testSummary(action: Action<TestSummaryStep>) {
        configureAndAdd("testSummary", action)
    }

    fun flakyReport(closure: Closure<FlakyReportStep>) {
        configureAndAdd("flakyReport", closure)
    }

    fun flakyReport(action: Action<FlakyReportStep>) {
        configureAndAdd("flakyReport", action)
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

    /**
     * @name any unique name of a step
     */
    fun customTask(name: String, configuration: Action<CustomTaskStep>) {
        configureAndAdd(name, configuration)
    }

    /**
     * @name any unique name of a step
     */
    fun customTask(name: String, configuration: Closure<CustomTaskStep>) {
        configureAndAdd(name, configuration)
    }

    fun artifacts(closure: Closure<ArtifactsConfiguration>) =
        artifacts(
            closure.toAction(delegate = artifactsConfig)
        )

    fun artifacts(action: Action<ArtifactsConfiguration>) {
        action.execute(artifactsConfig)
        val step = steps.maybeCreate("artifacts", VerifyArtifactsStep::class.java)
        step.useImpactAnalysis = this.useImpactAnalysis
    }

    fun <T : BuildStep> overrideStep(type: Class<T>, configuration: Closure<T>) =
        overrideStep(type, configuration.toAction())

    inline fun <reified T : BuildStep> overrideStep(configuration: Action<T>) =
        overrideStep(T::class.java, configuration)

    fun <T : BuildStep> overrideStep(type: Class<T>, configuration: Action<T>) {
        val steps = steps.withType(type).toList()
        require(steps.isNotEmpty()) {
            "Build step ${type.simpleName} is not registered in $buildStepListName"
        }
        require(steps.size == 1) {
            "Found multiple steps ${type.simpleName} in $buildStepListName. Please, specify the name."
        }
        val step = steps.first()
        configuration.execute(step)
    }

    fun <T : BuildStep> overrideStep(name: String, type: Class<T>, configuration: Closure<T>) =
        overrideStep(name, type, configuration.toAction())

    fun <T : BuildStep> overrideStep(name: String, type: Class<T>, configuration: Action<T>) {
        val step = requireNotNull(steps.findByName(name)) {
            "Build step '$name' is not registered in $buildStepListName"
        }
        require(type.isAssignableFrom(step::class.java)) {
            "Build step '$name' is ${step::class.java} but expected ${type.name}"
        }
        @Suppress("UNCHECKED_CAST")
        configuration.execute(step as T)
    }

    private inline fun <reified T : BuildStep> configureAndAdd(
        name: String,
        configuration: Closure<T>
    ) = configureAndAdd(name, configuration.toAction())

    private inline fun <reified T : BuildStep> configureAndAdd(
        name: String,
        configuration: Action<T>
    ) {
        ensureNotRegistered(name)

        val step = steps.maybeCreate(name, T::class.java)

        configuration.execute(step)

        if (step is ImpactAnalysisAwareBuildStep) {
            step.useImpactAnalysis = this.useImpactAnalysis
        }
    }

    private fun ensureNotRegistered(name: String) {
        val step = steps.findByName(name)
        if (step != null && explicitOverride) {
            throw Problem.Builder(
                shortDescription = "Overriding existing build step '$name' (${step::class.java.simpleName}) " +
                    "in '$buildStepListName' chain",
                context = "Adding a build step '$name' to $buildStepListName"
            )
                .because("Forbid implicit overriding of build steps")
                .addSolution("Configure the build step in one place")
                .addSolution("Override the build step explicitly by overrideStep() method")
                .build()
                .asRuntimeException()
        }
    }

    private fun <T> Closure<T>.toAction(
        delegate: Any? = null
    ): Action<T> = Action<T> {
        this.delegate = delegate ?: it
        this.resolveStrategy = Closure.DELEGATE_FIRST
        this.call()
    }
}
