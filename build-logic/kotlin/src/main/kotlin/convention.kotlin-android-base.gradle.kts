import com.avito.android.withVersionCatalog
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin-android")
    id("convention.kotlin-base")
}

kotlin {
    explicitApi()
    jvmToolchain {
        withVersionCatalog { libs ->
            this.apply {
                languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
            }
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}
