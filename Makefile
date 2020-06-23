test_build_type?=debug
infra?=
ci?=false
log_level?=-q
kubernetesContext?=beta
localFilter?=
includePrefix?=
includeAnnotation?=
useCompositeBuild=true

params?=

ifdef localFilter
params +=-PlocalFilter=$(localFilter)
endif

ifdef includePrefix
params +=-PincludePrefix=$(includePrefix)
endif

ifdef includeAnnotation
params +=-PincludeAnnotation=$(includeAnnotation)
endif

ifdef infra
params +=-PinfraVersion=$(infra)
endif

ifeq ($(gradle_debug),true)
params +=-Dorg.gradle.jvmargs='-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=y' --no-daemon
endif

params +=-PtestBuildType=$(test_build_type)
params +=-Pci=$(ci)
params +=$(log_level)
params +=-PkubernetesContext=$(kubernetesContext)
params +=-PuseCompositeBuild=$(useCompositeBuild)

module=test-app

help:
	./gradlew help $(params)

assembleSamples:
	./gradlew samples:test-app:assembleAndroidTest samples:test-app-without-backward-compatibility:assembleAndroidTest

sample_app_help:
	./gradlew samples:$(module):help $(params)

publish_to_maven_local:
	./gradlew -p subprojects publishToMavenLocal -PprojectVersion=local $(log_level)

sample_app_instrumentation:
	./gradlew samples:$(module):instrumentationUi $(params)

sample_app_instrumentation_local:
	./gradlew samples:$(module):instrumentationLocal $(params)

sample_app_instrumentation_android_debug:
	./gradlew samples:$(module):instrumentationUiDebug $(params)

dynamic_properties:
	$(eval keepFailedTestsFromReport?=)
	$(eval skipSucceedTestsFromPreviousRun=true)
	$(eval testFilter?=empty)
	$(eval dynamicPrefixFilter?=)

sample_app_instrumentation_dynamic: dynamic_properties
	./gradlew samples:$(module):instrumentationDynamic -PinfraVersion=local -PtestBuildType=$(test_build_type) -PdynamicTarget22=true -Pinstrumentation.dynamic.testFilter=$(testFilter) -Pinstrumentation.dynamic.keepFailedTestsFromReport=$(keepFailedTestsFromReport) -Pinstrumentation.dynamic.skipSucceedTestsFromPreviousRun=$(skipSucceedTestsFromPreviousRun) -PdynamicPrefixFilter=$(skipTestsWithPrefix) $(log_level)

unit_tests:
	./gradlew test $(log_level)

clear_k8s_deployments_by_namespaces:
	./gradlew subprojects\:ci\:k8s-deployments-cleaner\:clearByNamespaces -PteamcityApiPassword=$(teamcityApiPassword) $(log_level)

clear_k8s_deployments_by_names:
	./gradlew subprojects\:ci\:k8s-deployments-cleaner\:deleteByNames -Pci=true $(log_level)
