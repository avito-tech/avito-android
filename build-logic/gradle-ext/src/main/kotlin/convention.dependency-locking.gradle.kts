val taskGroup = "Avito Android build"

val isLockingEnabled: Boolean = providers.gradleProperty("avito.dependencyLocking.enabled")
    .getOrElse("true")
    .toBoolean()

dependencyLocking {
    if (isLockingEnabled) {
        lockAllConfigurations()
    }
    lockMode.set(LockMode.DEFAULT)
    lockFile.set(file("$projectDir/locking/gradle.lockfile"))
}

tasks.register("resolveAndLockAll") {
    group = taskGroup
    description = "Resolve all dependencies and write locks"

    doFirst {
        require(gradle.startParameter.isWriteDependencyLocks) {
            "should be called with --write-locks flag"
        }
    }

    /**
     * Can't use "resolve" method, probably because of some project misconfiguration,
     * seems like dependencies will do the same
     *
     * see https://docs.gradle.org/current/userguide/dependency_locking.html#lock_all_configurations_in_one_build_execution
     */
    dependsOn(tasks.named("dependencies"), tasks.named("buildEnvironment"))
}
