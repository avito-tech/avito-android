package com.avito.android.proguard_guard.shadowr8

import com.android.build.gradle.internal.tasks.R8Task
import com.avito.logger.GradleLoggerPlugin

internal fun R8Task.print() {
    val logger = GradleLoggerPlugin.getLoggerFactory(this).create("R8TaskPrinter")

    with(logger) {
        info("disableDesugaring: ${toolParameters.disableDesugaring.orNull}")
        info("multiDexKeepFile: ${multiDexKeepFile.orNull}")
        info("multiDexKeepProguard: ${multiDexKeepProguard.orNull}")
        info("mainDexRulesFiles: ${mainDexRulesFiles.joinToString()}")
        info("bootClasspath: ${bootClasspath.joinToString()}")
        info("errorFormatMode: ${errorFormatMode.orNull}")
        info("minSdkVersion: ${toolParameters.minSdkVersion.orNull}")
        info("debuggable: ${toolParameters.debuggable.orNull}")
        info("disableTreeShaking: ${toolParameters.disableTreeShaking.orNull}")
        info("duplicateClassesCheck: ${duplicateClassesCheck.joinToString()}")
        info("disableMinification: ${toolParameters.disableMinification.orNull}")
        info("proguardConfigurations: ${proguardConfigurations.joinToString()}")
        info("fullMode: ${toolParameters.fullMode.orNull}")
//        info("dexingType: $dexingType")
        info("featureClassJars: ${featureClassJars.joinToString()}")
        info("featureJavaResourceJars: ${featureJavaResourceJars.joinToString()}")
        info("baseJar: ${baseJar.orNull}")
        info("coreLibDesugarConfig: ${coreLibDesugarConfig.orNull}")
        info("outputClasses: ${outputClasses.orNull}")
        info("outputDex: ${outputDex.orNull}")
//        info("projectOutputKeepRules: ${projectOutputKeepRules.orNull}")
        info("featureDexDir: ${featureDexDir.orNull}")
        info("featureJavaResourceOutputDir: ${featureJavaResourceOutputDir.orNull}")
        info("outputResources: ${outputResources.orNull}")
        info("Start ProguardConfigurableTask")
        info("variantName: $variantName")
        info("shrinkingWithDynamicFeatures: ${shrinkingWithDynamicFeatures.orNull}")
        info("testedMappingFile: ${testedMappingFile.joinToString()}")
        info("classes: ${classes.joinToString()}")
//        info("resources: ${resources.joinToString()}")
        info("referencedClasses: ${referencedClasses.joinToString()}")
        info("referencedResources: ${referencedResources.joinToString()}")
        info("extractedDefaultProguardFile: ${extractedDefaultProguardFile.orNull}")
        info("configurationFiles: ${configurationFiles.joinToString()}")
        info("mappingFile: ${mappingFile.orNull}")
    }
}
