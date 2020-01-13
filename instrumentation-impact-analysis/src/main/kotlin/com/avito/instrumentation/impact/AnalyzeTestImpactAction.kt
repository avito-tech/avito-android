package com.avito.instrumentation.impact

import com.avito.android.isAndroid
import com.avito.bytecode.DIRTY_STUB
import com.avito.impact.ModifiedProject
import com.avito.impact.ModifiedProjectsFinder
import com.avito.impact.ReportType
import com.avito.impact.ScreenToPackageRelationFinder
import com.avito.impact.configuration.internalModule
import com.avito.impact.util.AndroidPackage
import com.avito.impact.util.AndroidProject
import com.avito.impact.util.Test
import com.avito.instrumentation.impact.model.AffectionType
import com.avito.utils.logging.CILogger

internal data class ImpactSummary(
    val affectedPackages: AffectedPackages,
    val affectedTests: AffectedTests,
    val allTests: Set<Test>,
    val addedTests: Set<Test>,
    val skippedTests: Set<Test>,
    val testsToRun: Set<Test>
) {
    data class AffectedPackages(
        val implementation: Set<AndroidPackage>,
        val androidTestImplementation: Set<AndroidPackage>
    )

    data class AffectedTests(
        val implementation: Set<Test>,
        val androidTestImplementation: Set<Test>,
        val codeChanges: Set<Test>
    )
}

internal class AnalyzeTestImpactAction(
    private val targetModule: AndroidProject,
    private val bytecodeAnalyzeSummary: BytecodeAnalyzeSummary,
    private val packageFilter: String?,
    private val finder: ModifiedProjectsFinder,
    private val ciLogger: CILogger
) {

    private val screenToPackageRelationFinder: ScreenToPackageRelationFinder = ScreenToPackageRelationFinder.Impl(
        targetModule = targetModule,
        screenToViewId = bytecodeAnalyzeSummary.rootIdByScreen
    )

    private val tests: Set<Test> = bytecodeAnalyzeSummary
        .testsByScreen
        .values
        .flatten()
        .toSet()

    private val affectedByCodeChanges = bytecodeAnalyzeSummary
        .testsModifiedByUser
        .plus(bytecodeAnalyzeSummary.testsAffectedByDependentOnUserChangedCode)
        .map { it.methods }
        .flatten()
        .toSet()

    private val addedTests: Set<Test> = bytecodeAnalyzeSummary
        .testsModifiedByUser
        .filter { it.affectionType == AffectionType.TEST_ADDED }
        .flatMap { it.methods }
        .toSet()

    private val filteredTest =
        if (packageFilter != null) tests.filter { it.startsWith(packageFilter) }.toSet() else tests


    fun computeImpact(): ImpactSummary {
        val affectedImplProjects = findModifiedAndroidProjects(
            finder.findModifiedProjects(ReportType.IMPLEMENTATION)
        )
        val affectedAndroidTestProjects = findModifiedAndroidProjects(
            @Suppress("DEPRECATION")
            finder.findModifiedProjectsWithoutDependencyToAnotherConfigurations(ReportType.ANDROID_TESTS)
        )

        val affectedTestsByImpl = getAffectedTestsByChangedPackages(
            changedPackages = affectedImplProjects.map { it.debug.manifest.getPackage() }.toSet()
        )

        val affectedTestsByAndroidTest = getAffectedTestsByAndroidTest(
            affectedAndroidTestProjects = affectedAndroidTestProjects
        )

        val testsToRun: Set<Test> =
            affectedTestsByImpl + affectedTestsByAndroidTest + affectedByCodeChanges
        val skippedTests: Set<Test> = filteredTest - testsToRun

        return ImpactSummary(
            affectedPackages = ImpactSummary.AffectedPackages(
                implementation = affectedImplProjects.map { it.toString() }.toSet(),
                androidTestImplementation = affectedAndroidTestProjects.map { it.toString() }.toSet()
            ),
            affectedTests = ImpactSummary.AffectedTests(
                implementation = affectedTestsByImpl,
                androidTestImplementation = affectedTestsByAndroidTest,
                codeChanges = affectedByCodeChanges
            ),
            allTests = filteredTest,
            addedTests = addedTests,
            skippedTests = skippedTests,
            testsToRun = testsToRun
        )
    }

    private fun getAffectedTestsByAndroidTest(
        affectedAndroidTestProjects: List<AndroidProject>
    ): Set<Test> {
        val isTargetModuleAffected = affectedAndroidTestProjects.any { it.path == targetModule.path }
        val targetModuleHasModifiedAndroidTestDependency =
            targetModule.internalModule.getConfiguration(ReportType.ANDROID_TESTS).dependencies.any { it.isModified }
        return if (isTargetModuleAffected && targetModuleHasModifiedAndroidTestDependency) {
            filteredTest
        } else {
            getAffectedTestsByChangedPackages(
                changedPackages = affectedAndroidTestProjects.map { it.debug.manifest.getPackage() }.toSet()
            )
        }
    }

    private fun getAffectedTestsByChangedPackages(
        changedPackages: Set<String>
    ): Set<Test> {

        return bytecodeAnalyzeSummary
            .testsByScreen
            .flatMap { (screen, tests) ->
                //We couldn't determine screen to test relation, so we will run all of these tests
                if (screen == DIRTY_STUB) {
                    return@flatMap tests
                }

                when (val screenToPackageRelation = screenToPackageRelationFinder.find(screen)) {
                    is ScreenToPackageRelationFinder.Result.Found -> if (changedPackages.contains(
                            screenToPackageRelation.pkg
                        )
                    ) tests else emptySet()
                    is ScreenToPackageRelationFinder.Result.NotFound -> {
                        ciLogger.info("Failed NotFound: $screen -> []")
                        tests
                    }
                    is ScreenToPackageRelationFinder.Result.FoundInTargetModuleOnly -> {
                        ciLogger.info("Failed FoundInTargetModuleOnly: $screen -> [${screenToPackageRelation.targetModulePackage}]")
                        tests
                    }
                    is ScreenToPackageRelationFinder.Result.MultiplePackagesFound -> {
                        val anyPackageAffected =
                            screenToPackageRelation.packages.any { pkg -> changedPackages.contains(pkg) }
                        ciLogger.info("MultiplePackages: $screen -> [${screenToPackageRelation.packages.joinToString(", ")}]")
                        if (anyPackageAffected) tests else emptySet()
                    }
                }
            }.toSet()
    }

    private fun findModifiedAndroidProjects(projects: Set<ModifiedProject>) =
        projects
            .map { it.project }
            .filter { it.isAndroid() }
            .map { AndroidProject(it) }
}
