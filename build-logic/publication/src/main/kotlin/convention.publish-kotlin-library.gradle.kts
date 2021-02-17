import com.avito.android.publish.KotlinLibraryPublishExtension

plugins {
    id("convention.bintray")
}

val publishExtension = extensions.create<KotlinLibraryPublishExtension>("publish")

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
                artifactId = if (publishExtension.artifactId.isNotBlank()) {
                    publishExtension.artifactId
                } else {
                    project.name
                }
            }
        }
    }
}
