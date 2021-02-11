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
	./gradlew clean

publish_to_maven_local:
	./gradlew publishToMavenLocal -PprojectVersion=local $(log_level)

publish_to_artifactory:
	./gradlew publishToArtifactory -PprojectVersion=$(version) $(log_level)

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

dynamic_properties:
	$(eval keepFailedTestsFromReport?=)
	$(eval skipSucceedTestsFromPreviousRun=true)
	$(eval testFilter?=empty)
	$(eval dynamicPrefixFilter?=)

unit_tests:
	./gradlew test $(log_level)

integration_tests:
	./gradlew $(module):integrationTest

compile_tests:
	./gradlew compileTestKotlin $(log_level)

compile:
	./gradlew compileKotlin compileTestKotlin compileIntegTestKotlin $(log_level)

check:
	./gradlew check

detekt:
	./gradlew detektAll

.PHONY: docs
docs:
	./ci/documentation/preview.sh

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
