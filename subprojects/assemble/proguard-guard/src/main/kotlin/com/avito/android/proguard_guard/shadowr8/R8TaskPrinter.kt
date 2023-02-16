package com.avito.android.proguard_guard.shadowr8

import com.android.build.gradle.internal.tasks.R8Task
import com.avito.logger.GradleLoggerPlugin

internal fun R8Task.print() {
    val logger = GradleLoggerPlugin.getLoggerFactory(this).get().create("R8TaskPrinter")

    with(logger) {
        info("enableDesugaring: ${enableDesugaring.orNull}")
        info("multiDexKeepFile: ${multiDexKeepFile.orNull}")
        info("multiDexKeepProguard: ${multiDexKeepProguard.orNull}")
        info("mainDexRulesFiles: ${mainDexRulesFiles.joinToString()}")
        info("bootClasspath: ${bootClasspath.joinToString()}")
        info("errorFormatMode: ${errorFormatMode.orNull}")
        info("minSdkVersion: ${minSdkVersion.orNull}")
        info("debuggable: ${debuggable.orNull}")
        info("disableTreeShaking: ${disableTreeShaking.orNull}")
        info("duplicateClassesCheck: ${duplicateClassesCheck.joinToString()}")
        info("disableMinification: ${disableMinification.orNull}")
        info("proguardConfigurations: ${proguardConfigurations.joinToString()}")
        info("useFullR8: ${useFullR8.orNull}")
        info("dexingType: $dexingType")
        info("featureClassJars: ${featureClassJars.joinToString()}")
        info("featureJavaResourceJars: ${featureJavaResourceJars.joinToString()}")
        info("baseJar: ${baseJar.orNull}")
        info("coreLibDesugarConfig: ${coreLibDesugarConfig.orNull}")
        info("outputClasses: ${outputClasses.orNull}")
        info("outputDex: ${outputDex.orNull}")
        info("projectOutputKeepRules: ${projectOutputKeepRules.orNull}")
        info("baseDexDir: ${baseDexDir.orNull}")
        info("featureDexDir: ${featureDexDir.orNull}")
        info("featureJavaResourceOutputDir: ${featureJavaResourceOutputDir.orNull}")
        info("outputResources: ${outputResources.orNull}")
        info("Start ProguardConfigurableTask")
        info("variantName: $variantName")
        info("includeFeaturesInScopes: ${includeFeaturesInScopes.orNull}")
        info("testedMappingFile: ${testedMappingFile.joinToString()}")
        info("classes: ${classes.joinToString()}")
        info("resources: ${resources.joinToString()}")
        info("referencedClasses: ${referencedClasses.joinToString()}")
        info("referencedResources: ${referencedResources.joinToString()}")
        info("extractedDefaultProguardFile: ${extractedDefaultProguardFile.orNull}")
        info("configurationFiles: ${configurationFiles.joinToString()}")
        info("mappingFile: ${mappingFile.orNull}")
    }
}
