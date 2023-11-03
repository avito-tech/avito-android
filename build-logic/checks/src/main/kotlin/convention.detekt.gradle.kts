import com.avito.android.withVersionCatalog
import io.gitlab.arturbosch.detekt.Detekt

plugins {
    /**
     * https://docs.gradle.org/current/userguide/base_plugin.html
     * base plugin added to add wiring on check->build tasks
     */
    base
    id("io.gitlab.arturbosch.detekt")
}

// workaround for https://github.com/gradle/gradle/issues/15383
project.withVersionCatalog { libs ->
    dependencies {
        detektPlugins(libs.detektFormatting)
    }
}

val detekt = tasks.named<Detekt>("detekt") {
    description = "Check kt and kts files in a module"
    parallel = true
    setSource(
        files(
            project.layout.projectDirectory.dir("src"),
            project.layout.projectDirectory.file("build.gradle.kts"),
        )
    )

    /**
     * About config:
     * yaml is a copy of https://github.com/detekt/detekt/blob/master/detekt-core/src/main/resources/default-detekt-config.yml
     * all rules are disabled by default, enabled one by one
     */
    config.setFrom(files(project.rootDir.resolve("conf/detekt.yml")))
    buildUponDefaultConfig = false

    include("**/*.kt")
    include("**/*.kts")
    reports {
        xml.enabled = false
        html.enabled = false
    }
}

tasks.named("check").configure {
    dependsOn(detekt)
}
