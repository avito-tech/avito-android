plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(libs.kotlinCompilerEmbeddable)

    testImplementation(projects.subprojects.gradle.testProject) {
        because("File extensions") // todo probably move to :common:files
    }
}
