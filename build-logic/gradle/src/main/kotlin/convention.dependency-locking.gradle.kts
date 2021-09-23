val taskGroup = "Avito Android build"

val configurationsToLock = setOf("compileClasspath", "runtimeClasspath")

configurations {
    matching { shouldBeLocked(it) }
        .configureEach {
            resolutionStrategy.activateDependencyLocking()
        }
}

dependencyLocking {
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

    doLast {
        configurations
            .filter { it.isCanBeResolved && shouldBeLocked(it) }
            .forEach { it.resolve() }
    }
}

fun shouldBeLocked(configuration: Configuration): Boolean {
    return configurationsToLock.any { confToLock ->
        configuration.name.contains(confToLock, ignoreCase = true)
    }
}
