import com.avito.android.withVersionCatalog
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("convention.kotlin-jvm-base")
    id("convention.detekt")
}

withVersionCatalog { libs ->
    val javaTarget = JavaLanguageVersion.of(libs.versions.java.get()).toString()

    tasks.withType<JavaCompile> {
        sourceCompatibility = javaTarget
        targetCompatibility = javaTarget
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = javaTarget
        }
    }
}
