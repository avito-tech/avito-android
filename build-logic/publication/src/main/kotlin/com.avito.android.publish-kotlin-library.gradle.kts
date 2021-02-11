plugins {
    id("com.avito.android.bintray")
}

plugins.withId("kotlin") {
    extensions.getByType<JavaPluginExtension>().run {

        @Suppress("UnstableApiUsage")
        withSourcesJar()
    }
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            from(components["java"])
            afterEvaluate {
                artifactId = project.getOptionalExtra("artifact-id") ?: project.name
            }
        }
    }
}

fun Project.getOptionalExtra(key: String): String? {
    return if (extra.has(key)) {
        (extra[key] as? String)?.let { if (it.isBlank()) null else it }
    } else {
        null
    }
}
