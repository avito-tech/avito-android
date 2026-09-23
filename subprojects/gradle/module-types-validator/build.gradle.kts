import org.gradle.plugin.devel.tasks.PluginUnderTestMetadata

plugins {
    id("convention.kotlin-jvm")
    id("convention.publish-gradle-plugin")
    id("convention.gradle-testing")
    id("convention.test-fixtures")
    id("convention.ksp")
}

val androidPluginTestClasspath = configurations.create("androidPluginTestClasspath") {
    isCanBeConsumed = false
}

dependencies {
    api(project(":subprojects:gradle:module-types-api"))
    implementation(project(":subprojects:gradle:module-types")) {
        because("Applying ModuleTypesPlugin from this plugin")
    }

    implementation(project(":subprojects:gradle:gradle-extensions"))
    implementation(project(":subprojects:common:problem"))
    implementation(project(":subprojects:common:result"))
    compileOnly(libs.androidGradleApi)

    implementation(libs.moshi)
    ksp(libs.moshiCodegen)

    gradleTestImplementation(project(":subprojects:gradle:test-project"))
    add(androidPluginTestClasspath.name, libs.androidGradle)
}

tasks.named<PluginUnderTestMetadata>("pluginUnderTestMetadata") {
    pluginClasspath.from(androidPluginTestClasspath)
}

gradlePlugin {
    plugins {
        create("validationsModuleTypes") {
            id = "com.avito.android.module-types-validator"
            implementationClass =
                "com.avito.android.module_type.validation.ModuleTypeValidationPlugin"
            displayName = "Module types validations"
        }
    }
}
