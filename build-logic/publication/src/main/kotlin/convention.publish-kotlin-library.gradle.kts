import com.avito.android.publish.KotlinLibraryPublishExtension

plugins {
    id("convention.publish-release")
    id("convention.publish-artifactory")
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
        register<MavenPublication>("kotlin-library-maven") {
            from(components["java"])

            afterEvaluate {
                artifactId = publishExtension.artifactId.getOrElse(project.name)
            }
        }
    }
}
