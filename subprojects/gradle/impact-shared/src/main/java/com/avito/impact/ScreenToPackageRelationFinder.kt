package com.avito.impact

import com.avito.impact.util.AndroidPackage
import com.avito.impact.util.AndroidProject
import com.avito.impact.util.RootId
import com.avito.impact.util.Screen

interface ScreenToPackageRelationFinder {

    sealed class Result {
        class Found(val pkg: AndroidPackage) : Result()
        object NotFound : Result()
        class FoundInTargetModuleOnly(val targetModulePackage: AndroidPackage) : Result()
        class MultiplePackagesFound(val packages: Set<AndroidPackage>) : Result()
    }

    fun find(screen: Screen): Result

    class Impl(
        targetModule: AndroidProject,
        private val screenToViewId: Map<Screen, RootId>
    ) :
        ScreenToPackageRelationFinder {

        private val targetModuleR = targetModule.debug.resourceSymbolList
        private val targetModulePackage = targetModule.debug.manifest.getPackage()

        private val screenToPackages: Map<Screen, List<AndroidPackage>> by lazy {
            screenToViewId
                .mapValues { (_, decimalId) ->
                    //todo test it!!
                    if (targetModuleR != null && targetModuleR.contains(decimalId)) {
                        listOf(targetModulePackage)
                    } else {
                        emptyList()
                    }
                }
        }

        override fun find(screen: Screen): Result {
            val packagesWithScreenRootIdFound = screenToPackages[screen]?.toSet() ?: emptySet()

            //Assume that target module doesn't have features
            val withoutTarget: Set<AndroidPackage> = packagesWithScreenRootIdFound - targetModulePackage

            return when {
                withoutTarget.size == 1 -> Result.Found(withoutTarget.first())
                withoutTarget.size > 1 -> Result.MultiplePackagesFound(withoutTarget)
                else -> if (withoutTarget.size == packagesWithScreenRootIdFound.size) {
                    Result.NotFound
                } else Result.FoundInTargetModuleOnly(targetModulePackage)
            }
        }
    }
}
