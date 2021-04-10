import com.android.build.gradle.internal.tasks.ProcessJavaResTask
import java.util.jar.Attributes

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("convention.kotlin-base")
    id("convention.android-base")
}

val generatedJavaResDir = project.layout.buildDirectory.file("generated/avito/java_res")

android {

    /**
     * Ignore all buildTypes instead of release for com.android.library modules
     * Also configure fallbacks for dependent modules
     */
    variantFilter {
        if (name != "release") {
            ignore = true
        }
    }

    @Suppress("UnstableApiUsage")
    onVariants {
        androidTest {
            enabled = false
        }
    }

    sourceSets {
        getByName("main").resources.srcDir(generatedJavaResDir.get().asFile)
    }
}

val generateLibraryJavaResProvider: TaskProvider<WriteProperties> =
    project.tasks.register<WriteProperties>("generateLibraryJavaRes") {
        // Don't use MANIFEST.MF to avoid clashing and rewriting in packaging
        val projectUniqueProperties = "META-INF/com.avito.android.${project.name}.properties"
        outputFile = File(generatedJavaResDir.get().asFile, projectUniqueProperties)

        property(Attributes.Name.IMPLEMENTATION_VERSION.toString(), project.version.toString())
    }

project.tasks.withType<ProcessJavaResTask>().configureEach {
    dependsOn(generateLibraryJavaResProvider)
}
