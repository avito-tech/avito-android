package com.avito.android.proguard_guard.configuration

import com.avito.logger.GradleLoggerPlugin
import org.gradle.api.Task
import proguard.ClassPath
import proguard.ClassPathEntry
import proguard.ClassSpecification
import proguard.Configuration
import proguard.KeepClassSpecification
import proguard.MemberSpecification

internal fun Configuration.print(task: Task) {
    val logger = GradleLoggerPlugin.getLoggerFactory(task).get().create("ProguardConfigurationPrinter")
    logger.info(this.asString())
}

internal fun Configuration.asString(): String {
    return "Configuration(" +
        "programJars=${programJars?.asString()},\n" +
        "libraryJars=${libraryJars?.asString()},\n" +
        "skipNonPublicLibraryClasses=$skipNonPublicLibraryClasses,\n" +
        "skipNonPublicLibraryClassMembers=$skipNonPublicLibraryClassMembers,\n" +
        "keepDirectories=$keepDirectories,\n" +
        "targetClassVersion=$targetClassVersion,\n" +
        "lastModified=$lastModified,\n" +
        "keep=${keepClassSpecificationString(keep)},\n" +
        "printSeeds=$printSeeds,\n" +
        "shrink=$shrink,\n" +
        "printUsage=$printUsage,\n" +
        "whyAreYouKeeping=${classSpecificationString(whyAreYouKeeping)},\n" +
        "optimize=$optimize,\n" +
        "optimizations=$optimizations,\n" +
        "optimizationPasses=$optimizationPasses,\n" +
        "assumeNoSideEffects=${classSpecificationString(assumeNoSideEffects)},\n" +
        "assumeNoExternalSideEffects=${classSpecificationString(assumeNoExternalSideEffects)},\n" +
        "assumeNoEscapingParameters=${classSpecificationString(assumeNoEscapingParameters)},\n" +
        "assumeNoExternalReturnValues=${classSpecificationString(assumeNoExternalReturnValues)},\n" +
        "assumeValues=${classSpecificationString(assumeValues)},\n" +
        "allowAccessModification=$allowAccessModification,\n" +
        "mergeInterfacesAggressively=$mergeInterfacesAggressively,\n" +
        "obfuscate=$obfuscate,\n" +
        "printMapping=$printMapping,\n" +
        "applyMapping=$applyMapping,\n" +
        "obfuscationDictionary=$obfuscationDictionary,\n" +
        "classObfuscationDictionary=$classObfuscationDictionary,\n" +
        "packageObfuscationDictionary=$packageObfuscationDictionary,\n" +
        "overloadAggressively=$overloadAggressively,\n" +
        "useUniqueClassMemberNames=$useUniqueClassMemberNames,\n" +
        "useMixedCaseClassNames=$useMixedCaseClassNames,\n" +
        "keepPackageNames=$keepPackageNames,\n" +
        "flattenPackageHierarchy=$flattenPackageHierarchy,\n" +
        "repackageClasses=$repackageClasses,\n" +
        "keepAttributes=$keepAttributes,\n" +
        "keepParameterNames=$keepParameterNames,\n" +
        "newSourceFileAttribute=$newSourceFileAttribute,\n" +
        "adaptClassStrings=$adaptClassStrings,\n" +
        "adaptResourceFileNames=$adaptResourceFileNames,\n" +
        "adaptResourceFileContents=$adaptResourceFileContents,\n" +
        "preverify=$preverify,\n" +
        "microEdition=$microEdition,\n" +
        "android=$android,\n" +
        "verbose=$verbose,\n" +
        "note=$note,\n" +
        "warn=$warn,\n" +
        "ignoreWarnings=$ignoreWarnings,\n" +
        "printConfiguration=$printConfiguration,\n" +
        "dump=$dump,\n" +
        "addConfigurationDebugging=$addConfigurationDebugging,\n" +
        "backport=$backport" +
        ")"
}

private fun KeepClassSpecification.asString(): String {
    return "KeepClassSpecification(" +
        "markClasses=$markClasses, " +
        "markConditionally=$markConditionally, " +
        "markDescriptorClasses=$markDescriptorClasses, " +
        "markCodeAttributes=$markCodeAttributes, " +
        "allowShrinking=$allowShrinking, " +
        "allowOptimization=$allowOptimization, " +
        "allowObfuscation=$allowObfuscation, " +
        "condition=${condition?.asString()}, " +
//        "comments=$comments, " +
        "requiredSetAccessFlags=$requiredSetAccessFlags, " +
        "requiredUnsetAccessFlags=$requiredUnsetAccessFlags, " +
        "annotationType=$annotationType, " +
        "className=$className, " +
        "extendsAnnotationType=$extendsAnnotationType, " +
        "extendsClassName=$extendsClassName, " +
        "attributeNames=$attributeNames, " +
        "fieldSpecifications=${memberSpecificationString(fieldSpecifications)}, " +
        "methodSpecifications=${memberSpecificationString(methodSpecifications)}" +
        ")"
}

private fun ClassSpecification.asString(): String {
    return "ClassSpecification(" +
//        "comments=$comments, " +
        "requiredSetAccessFlags=$requiredSetAccessFlags, " +
        "requiredUnsetAccessFlags=$requiredUnsetAccessFlags, " +
        "annotationType=$annotationType, " +
        "className=$className, " +
        "extendsAnnotationType=$extendsAnnotationType, " +
        "extendsClassName=$extendsClassName, " +
        "attributeNames=$attributeNames, " +
        "fieldSpecifications=${memberSpecificationString(fieldSpecifications)}, " +
        "methodSpecifications=${memberSpecificationString(methodSpecifications)}" +
        ")"
}

private fun MemberSpecification.asString(): String {
    return "MemberSpecification(" +
        "requiredSetAccessFlags=$requiredSetAccessFlags, " +
        "requiredUnsetAccessFlags=$requiredUnsetAccessFlags, " +
        "annotationType=$annotationType, " +
        "name=$name, " +
        "descriptor=$descriptor, " +
        "attributeNames=$attributeNames" +
        ")"
}

private fun ClassPath.asString(): String {
    val classPath = this
    val classPathEntriesCopy = buildList<ClassPathEntry>(classPath.size()) {
        (0 until size()).forEach { index ->
            add(classPath.get(index))
        }
    }
    return "ClassPath(" +
        "classPathEntries=${classPathEntriesCopy.joinToString { it.asString() } }" +
        ")"
}

private fun ClassPathEntry.asString(): String {
    return "ClassPathEntry(" +
        "file=$file, " +
        "output=$isOutput, " +
        "filter=$filter, " +
        "apkFilter=$apkFilter, " +
        "jarFilter=$jarFilter, " +
        "aarFilter=$aarFilter, " +
        "warFilter=$warFilter, " +
        "earFilter=$earFilter, " +
        "jmodFilter=$jmodFilter, " +
        "zipFilter=$zipFilter" +
        ")"
}

private fun keepClassSpecificationString(keepList: List<Any?>?): String {
    return keepList?.joinToString {
        it as KeepClassSpecification
        it.asString()
    }.toString()
}

private fun classSpecificationString(classList: List<Any?>?): String {
    return classList?.joinToString {
        it as ClassSpecification
        it.asString()
    }.toString()
}

private fun memberSpecificationString(memberList: List<Any?>?): String {
    return memberList?.joinToString {
        it as MemberSpecification
        it.asString()
    }.toString()
}
