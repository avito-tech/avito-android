plugins {
    base
    // accessing version catalog here will be supported in grale 7.2
    // https://github.com/gradle/gradle/pull/17394
    id("io.gitlab.arturbosch.detekt") version "1.16.0"
}

val detektAll = tasks.register<io.gitlab.arturbosch.detekt.Detekt>("detektAll") {
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

tasks.named("check").configure {
    dependsOn(detektAll)
}
