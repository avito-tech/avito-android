package com.avito.android.baseline_profile

data class ProjectSetup(
    val pluginTaskName: String = "applyBaselineProfile",
    val instrumentationTaskName: String = "instrumentationGenerateProfileKubernetes",
    val appModuleName: String = "application",
    val testModuleName: String = "macrobenchmarks",
    val applicationVariant: String = "benchmark",
    val testPackageName: String = "com.example.macrobenchmark",
    val generatedProfileFileName: String = "some-date-prefix-2023-12-12-baseline-prof.txt",
    val saveToVersionControl: Boolean = false,
    val checkoutBranchName: String = "develop",
)
