package com.avito.android.module_type.internal

import com.avito.android.module_type.ApplicationDeclaration

/**
 * Finds `sharedBetweenApps` flags that no direct cross-application dependency justifies. A flag on a
 * common module is always redundant; transitive reachability never justifies a flag.
 */
internal class UnusedSharedBetweenAppsFinder(
    private val moduleDescriptions: Set<ModuleDescription>,
    private val commonApp: ApplicationDeclaration,
    private val sharingApps: Set<ApplicationDeclaration>,
) {

    fun findUnusedFlags(): UnusedFlags {
        val pathsToSharedModules = moduleDescriptions
            .filter { it.module.sharedBetweenApps }
            .associateBy { it.module.path }

        if (pathsToSharedModules.isEmpty()) return UnusedFlags.EMPTY

        val justifiedPaths = mutableSetOf<String>()
        moduleDescriptions.forEach { consumer ->
            val consumerApp = consumer.module.type.app
            // Mirrors BetweenDifferentAppsRestriction: only a code-sharing or shared consumer benefits
            // from the flag; a common consumer stays restricted regardless of it.
            val consumerCanUseShared =
                consumerApp != commonApp && (consumerApp in sharingApps || consumer.module.sharedBetweenApps)
            if (!consumerCanUseShared) return@forEach

            consumer.directDependencies.values
                .asSequence()
                .flatten()
                .forEach { dependencyPath ->
                    if (dependencyPath in justifiedPaths) return@forEach
                    val dependency = pathsToSharedModules[dependencyPath] ?: return@forEach
                    // A same-app edge needs no flag, so only a cross-app edge justifies one.
                    if (dependency.module.type.app != consumerApp) {
                        justifiedPaths.add(dependencyPath)
                    }
                }
        }

        val redundantCommonModules = mutableListOf<String>()
        val unjustifiedModules = mutableListOf<String>()
        pathsToSharedModules.values.forEach { description ->
            val path = description.module.path
            when {
                // The flag on a common module is redundant regardless of consumers: common modules are
                // available to every application without any flag (see BetweenDifferentAppsRestriction).
                description.module.type.app == commonApp -> redundantCommonModules += path
                path !in justifiedPaths -> unjustifiedModules += path
            }
        }
        return UnusedFlags(
            redundantCommonModules = redundantCommonModules.sorted(),
            unjustifiedModules = unjustifiedModules.sorted(),
        )
    }

    internal data class UnusedFlags(
        val redundantCommonModules: List<String>,
        val unjustifiedModules: List<String>,
    ) {
        val isEmpty: Boolean = redundantCommonModules.isEmpty() && unjustifiedModules.isEmpty()

        internal companion object {
            val EMPTY = UnusedFlags(emptyList(), emptyList())
        }
    }
}
