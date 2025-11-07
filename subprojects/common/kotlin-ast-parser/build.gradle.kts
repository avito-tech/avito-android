plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    compileOnly(libs.kotlinCompilerEmbeddable) {
        because("https://kotlinlang.org/docs/whatsnew21.html#compiler-symbols-hidden-from-the-kotlin-gradle-plugin-api")
    }

    testImplementation(libs.kotlinCompilerEmbeddable) {
        because("These classes are needed in testRuntimeClasspath to avoid ClassNotFoundException")
    }
    testImplementation(project(":subprojects:gradle:test-project")) {
        because("File extensions") // todo probably move to :common:files
    }
}
