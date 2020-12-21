SHELL := /bin/bash

test_build_type?=debug
infra?=
ci?=false
log_level?=-q
kubernetesContext?=beta
testFilter?=
includePrefix?=
includeAnnotation?=
useCompositeBuild=true
dry_run=false
instrumentation=Ui
stacktrace?=

params?=

ifdef testFilter
params +=-PcustomFilter=$(testFilter)
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

params +=-PtestBuildType=$(test_build_type)
params +=-Pci=$(ci)
params +=$(log_level)
params +=-PkubernetesContext=$(kubernetesContext)
params +=-PuseCompositeBuild=$(useCompositeBuild)

ifeq ($(gradle_debug),true)
params +=-Dorg.gradle.debug=true --no-daemon
endif

ifeq ($(dry_run),true)
params +=--dry-run
endif

ifdef stacktrace
params +=--stacktrace
endif

# from: https://stackoverflow.com/questions/10858261/abort-makefile-if-variable-not-set
#
# Check that given variables are set and all have non-empty values,
# die with an error otherwise.
#
# Params:
#   1. Variable name(s) to test.
#   2. (optional) Error message to print.
check_defined = \
    $(strip $(foreach 1,$1, \
        $(call __check_defined,$1,$(strip $(value 2)))))
__check_defined = \
    $(if $(value $1),, \
      $(error Undefined $1$(if $2, ($2))))

help:
	./gradlew help $(params)

clean:
	./gradlew -p subprojects clean

assembleSamples:
	./gradlew samples:test-app:assembleAndroidTest samples:test-app-without-backward-compatibility:assembleAndroidTest

sample_app_help:
	./gradlew samples:$(module):help $(params)

publish_to_maven_local:
	./gradlew -p subprojects publishToMavenLocal -PprojectVersion=local $(log_level)

publish_to_artifactory:
	./gradlew -p subprojects publishToArtifactory -PprojectVersion=$(version) $(log_level)

# precondition:
# - installed CLI: https://cli.github.com/
# - push $(version) branch
#
# post actions:
# - go to link in output
# - edit notes
# - publish release
draft_release:
	$(call check_defined, version)
	$(call check_defined, prev_version)
	gh release create $(version) \
		--draft \
		--target $(version) \
		--title $(version) \
		--notes "$$(git log --pretty=format:%s $(prev_version)..$(version) | cat)"

sample_app_instrumentation:
	./gradlew samples:$(module):instrumentation$(instrumentation) $(params)

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
	./gradlew -p subprojects test $(log_level)

integration_tests:
	./gradlew -p subprojects $(module):integrationTest

compile_tests:
	./gradlew -p subprojects compileTestKotlin $(log_level)

compile:
	./gradlew -p subprojects compileKotlin compileTestKotlin $(log_level)

check:
	./gradlew -p subprojects $(module):check

detekt:
	./gradlew -p subprojects detektAll

clear_k8s_deployments_by_namespaces:
	./gradlew subprojects\:ci\:k8s-deployments-cleaner\:clearByNamespaces -PteamcityApiPassword=$(teamcityApiPassword) $(log_level)

clear_k8s_deployments_by_names:
	./gradlew subprojects\:ci\:k8s-deployments-cleaner\:deleteByNames -Pci=true $(log_level)

record_screenshots:
	./gradlew samples:test-app-screenshot-test:clearScreenshots
	./gradlew samples:test-app-screenshot-test:connectedAndroidTest \
        -Pandroid.testInstrumentationRunnerArguments.annotation=com.avito.android.test.annotations.ScreenshotTest \
        -Pandroid.testInstrumentationRunnerArguments.recordScreenshots=true
	./gradlew samples:test-app-screenshot-test:recordScreenshots

analyzeImpactOnSampleApp:
	./gradlew samples:test-app-impact:app:analyzeTestImpact -PtargetBranch=develop $(params)

# Clear local branches that not on remote
# from: https://stackoverflow.com/a/17029936/981330
unsafe_clear_local_branches:
	git fetch --prune && \
	git branch -r | awk '{print $$1}' | egrep -v -f /dev/fd/0 <(git branch -vv | grep origin) | \
	awk '{print $$1}' | xargs git branch -D
