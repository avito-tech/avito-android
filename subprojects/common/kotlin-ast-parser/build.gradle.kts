plugins {
    id("kotlin")
    `maven-publish`
    id("com.jfrog.bintray")
}

dependencies {
    implementation(Dependencies.kotlinCompilerEmbeddable)

    testImplementation(project(":subprojects:gradle:test-project")) {
        because("File extensions") // todo probably move to :common:files
    }
}
