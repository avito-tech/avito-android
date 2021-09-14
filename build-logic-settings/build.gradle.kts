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
}

fun Project.isPlainDir(): Boolean {
    return !file("build.gradle").exists() && !file("build.gradle.kts").exists()
}
