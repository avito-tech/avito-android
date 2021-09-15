import io.gitlab.arturbosch.detekt.Detekt

plugins {
    // accessing version catalog here is blocked by IDE false-postive error
    // https://youtrack.jetbrains.com/issue/KTIJ-19369
    id("io.gitlab.arturbosch.detekt") version "1.18.1"
}

val detektAllTask = tasks.register<Detekt>("detektAll") {
    description = "Runs over whole code base without the starting overhead for each module."
    parallel = true
    setSource(files(projectDir))

    /**
     * About config:
     * yaml is a copy of https://github.com/detekt/detekt/blob/master/detekt-core/src/main/resources/default-detekt-config.yml
     * all rules are disabled by default, enabled one by one
     */
    config.setFrom(files(project.rootDir.resolve("../conf/detekt.yml")))
    buildUponDefaultConfig = true

    include("**/*.kt")
    include("**/*.kts")
    exclude("**/resources/**")
    exclude("**/build/**")
    reports {
        xml.enabled = false
        html.enabled = false
    }
}

val taskGroup = "Avito Android build"

tasks.register("compileAll") {
    group = taskGroup
    description = "Compile all code of the '${project.path}' component"

    dependsOn(
        subprojects
            .filter { !it.isPlainDir() }
            .map { "${it.path}:compileKotlin" }
    )
}

tasks.register("assembleAll") {
    group = taskGroup
    description = "Assemble '${project.path}' component"
    dependsOn(
        subprojects
            .filter { !it.isPlainDir() }
            .map { "${it.path}:assemble" }
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

    dependsOn(detektAllTask)
}

fun Project.isPlainDir(): Boolean {
    return !file("build.gradle").exists() && !file("build.gradle.kts").exists()
}
