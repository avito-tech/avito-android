plugins {
    id("com.avito.android.kotlin-jvm")
    id("com.avito.android.publish-kotlin-library")
    `java-test-fixtures`
}

dependencies {
    api(project(":subprojects:common:logger"))
}
