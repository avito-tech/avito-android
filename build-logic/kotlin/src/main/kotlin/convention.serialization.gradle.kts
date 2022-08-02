import com.avito.android.withVersionCatalog
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}

// workaround for https://github.com/gradle/gradle/issues/15383
project.withVersionCatalog { libs ->
    dependencies {
        add("implementation", libs.kotlinx.serialization.json)
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs +
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi"
    }
}
