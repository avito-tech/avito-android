import com.android.build.gradle.LibraryExtension
import com.avito.android.publish.AndroidLibraryPublishExtension

plugins {
    id("convention.bintray")
}

val publishExtension = extensions.create<AndroidLibraryPublishExtension>("publish")

configure<LibraryExtension> {

    val sourcesTask = tasks.register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        from(sourceSets["main"].java.srcDirs)
    }

    val allVariantNames = mutableListOf<String>()
    var registeredVariants = 0

    libraryVariants
        .matching {
            allVariantNames += it.name
            it.name == publishExtension.variant.get()
        }
        .whenObjectAdded {
            publishing {
                publications {
                    register<MavenPublication>("android-library-${publishExtension.variant.get()}") {
                        from(components.getAt(publishExtension.variant.get()))
                        artifact(sourcesTask.get())

                        registeredVariants++
                    }
                }
            }
        }

    afterEvaluate {
        require(registeredVariants > 0) {
            "No publications was created for ${project.path}, " +
                "with plugin \"convention.publish-android-library\" added. Options: \n" +
                " - Remove plugin if library was not supposed to be published\n" +
                " - Check configuration: variant to be published \"${publishExtension.variant.get()}\"; " +
                "available variants=$allVariantNames"
        }
    }
}
