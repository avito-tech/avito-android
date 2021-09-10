val taskGroup = "Avito android build"

tasks.named<TaskReportTask>("tasks") {
    displayGroup = taskGroup
}

tasks.register("assembleAll") {
    group = taskGroup
    description = "Compile all code of the '${project.name}' component"
    dependsOn(
        subprojects
            .filter { !it.isJustADir() }
            .map { "${it.path}:assemble" }
    )
}

tasks.register("compileAll") {
    group = taskGroup
}

tasks.register("checkAll") {
    group = taskGroup
    description = "Compile all code and run all tests of the '${project.name}' component"
    dependsOn(
        subprojects
            .filter { !it.isJustADir() }
            .map { "${it.path}:check" }
    )
}

fun Project.isJustADir(): Boolean {
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
