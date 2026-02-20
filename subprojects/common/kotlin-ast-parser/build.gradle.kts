plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-kotlin-library")
}

dependencies {
    implementation(libs.kotlinCompilerEmbeddable) {
        because("AST parsing needs compiler at runtime; KGP 2.1 hides these symbols")
    }

    testImplementation(project(":subprojects:gradle:test-project")) {
        because("File extensions") // todo probably move to :common:files
    }
}
