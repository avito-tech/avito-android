val taskGroup = "Avito Android build"

tasks.register("assembleAll") {
    group = taskGroup
    description = "Assemble '${project.path}' component"
    dependsOn(
        subprojects
            .filter { !it.isPlainDir() }
            .map { "${it.path}:assemble" }
    )
}

tasks.register("compileAll") {
    group = taskGroup
    description = "Compile all code of the '${project.path}' component"

    dependsOn(
        subprojects
            .filter { !it.isPlainDir() }
            .map { "${it.path}:compileAll" }
    )
}

tasks.register("checkAll") {
    group = taskGroup
    description = "Run all tests and static analysis tools on '${project.path}' component"
    dependsOn(
        subprojects
            .filter { !it.isPlainDir() }
            .map { "${it.path}:check" }
    )
}

fun Project.isPlainDir(): Boolean {
    return !file("build.gradle").exists() && !file("build.gradle.kts").exists()
}

val parentBuild = gradle.parent

/**
 * --dry-run on root build executes tasks in a composite build
 * Workaround to https://github.com/gradle/gradle/issues/2517
 */
if (parentBuild != null && parentBuild.startParameter.isDryRun) {
    gradle.startParameter.isDryRun = true
}
