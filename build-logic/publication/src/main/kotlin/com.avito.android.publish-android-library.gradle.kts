import com.android.build.gradle.LibraryExtension

plugins {
    id("com.avito.android.bintray")
}

configure<LibraryExtension> {

    val sourcesTask = tasks.register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].java.srcDirs)
    }

    val publishingVariant = "release"

    libraryVariants
        .matching { it.name == publishingVariant }
        .whenObjectAdded {
            publishing {
                publications {
                    register<MavenPublication>(publishingVariant) {
                        from(components.getAt(publishingVariant))
                        artifact(sourcesTask.get())
                    }
                }
            }
        }
}
