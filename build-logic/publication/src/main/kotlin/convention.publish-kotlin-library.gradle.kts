import com.avito.android.publish.KotlinLibraryPublishExtension

plugins {
    id("convention.publish-kotlin-base")
    id("convention.publish-release")
}

val publishExtension = extensions.create<KotlinLibraryPublishExtension>("publish")

publishing {
    publications {
        register<MavenPublication>("kotlinLibraryMaven") {
            from(components["java"])

            afterEvaluate {
                artifactId = publishExtension.artifactId.getOrElse(project.name)
            }
        }
    }
}
