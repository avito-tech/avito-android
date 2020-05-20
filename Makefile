test_build_type?=debug
infra?=
ci?=false
log_level?=-q
kubernetesContext?=beta
localFilter?=
includePrefix?=
includeAnnotation?=

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

test_app_help:
	./gradlew s:android-test:test-app:help -PinfraVersion=local $(log_level)

publish_to_maven_local:
	./gradlew publishToMavenLocal -PprojectVersion=local $(log_level)

test_app_instrumentation:
	./gradlew subprojects\:android-test\:test-app\:instrumentationUi $(params)

test_app_instrumentation_local:
	./gradlew subprojects\:android-test\:test-app\:instrumentationLocal $(params)

test_app_instrumentation_android_debug:
	./gradlew :subprojects:android-test:test-app:instrumentationUiDebug $(params)

dynamic_properties:
	$(eval keepFailedTestsFromReport?=)
	$(eval skipSucceedTestsFromPreviousRun=true)
	$(eval testFilter?=empty)
	$(eval dynamicPrefixFilter?=)

test_app_instrumentation_dynamic: dynamic_properties
	./gradlew subprojects\:android-test\:test-app\:instrumentationDynamic -PinfraVersion=local -PtestBuildType=$(test_build_type) -PdynamicTarget22=true -Pinstrumentation.dynamic.testFilter=$(testFilter) -Pinstrumentation.dynamic.keepFailedTestsFromReport=$(keepFailedTestsFromReport) -Pinstrumentation.dynamic.skipSucceedTestsFromPreviousRun=$(skipSucceedTestsFromPreviousRun) -PdynamicPrefixFilter=$(skipTestsWithPrefix) $(log_level)

unit_tests:
	./gradlew test $(log_level)

clear_k8s_deployments_by_namespaces:
	./gradlew subprojects\:ci\:k8s-deployments-cleaner\:clearByNamespaces -PteamcityApiPassword=$(teamcityApiPassword) $(log_level)

clear_k8s_deployments_by_names:
	./gradlew subprojects\:ci\:k8s-deployments-cleaner\:deleteByNames -Pci=true $(log_level)
