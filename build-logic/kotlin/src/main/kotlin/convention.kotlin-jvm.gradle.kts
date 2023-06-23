import com.avito.android.withVersionCatalog
import java.util.jar.Attributes

plugins {
    id("kotlin")
    id("convention.kotlin-base")
    id("convention.dependency-locking-kotlin")
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

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(
            mapOf(
                // To access a build version in runtime through class.java.`package`.implementationVersion
                Attributes.Name.IMPLEMENTATION_VERSION.toString() to project.version
            )
        )
    }
}
