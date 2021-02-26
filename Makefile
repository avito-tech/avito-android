SHELL := /bin/bash

DOCKER_REGISTRY=registry.k.avito.ru
ANDROID_BUILDER_TAG=28e6bacd68
IMAGE_ANDROID_BUILDER=$(DOCKER_REGISTRY)/android/builder:$(ANDROID_BUILDER_TAG)
GRADLE_CACHE_DIR=$(HOME)/.gradle/caches
GRADLE_WRAPPER_DIR=$(HOME)/.gradle/wrapper
USER_ID=$(shell id -u $(USER))

docker=false
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
project=-p subprojects

docker_command?=

ifeq ($(docker),true)
define docker_command
docker run --rm \
	--volume "$(shell pwd)":/app \
	--volume "$(GRADLE_CACHE_DIR)":/gradle/caches \
	--volume "$(GRADLE_WRAPPER_DIR)":/gradle/wrapper \
	--workdir /app \
	--env TZ="Europe/Moscow" \
	--env LOCAL_USER_ID="$(USER_ID)" \
	--env GRADLE_USER_HOME=/gradle \
	android-builder
endef
endif

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

clean:
	rm -rf `find -type d -name build`

help:
	./gradlew $(project) $(log_level) help $(params)

publish_to_maven_local:
	./gradlew $(project) $(log_level) publishToMavenLocal -PprojectVersion=local

publish_to_artifactory:
	./gradlew $(project) $(log_level) publishToArtifactory -PprojectVersion=$(version) -Dorg.gradle.internal.publish.checksums.insecure=true

unit_tests:
	./gradlew $(project) $(log_level) test

gradle_test:
	./gradlew $(project) $(log_level) gradleTest

integration_tests:
	./gradlew $(project) $(log_level) integrationTest

compile_tests:
	./gradlew $(project) $(log_level) compileTestKotlin

compile:
	./gradlew $(project) $(log_level) compileAll

check:
	./gradlew $(project) $(log_level) check

fast_check:
	./gradlew $(project) $(log_level) compileAll detektAll test

clean_fast_check:
	make clean
	./gradlew $(project) $(log_level) compileAll detektAll test --rerun-tasks --no-build-cache

detekt:
	$(docker_command) ./gradlew $(project) $(log_level) detektAll

build_android_image:
	cd ./ci/docker/android-builder && \
	docker build -t android-builder .

.PHONY: docs
docs:
	./ci/documentation/lint.sh
	./ci/documentation/preview.sh

clear_k8s_deployments_by_namespaces:
	./gradlew subprojects\:ci\:k8s-deployments-cleaner\:clearByNamespaces -PteamcityApiPassword=$(teamcityApiPassword) $(log_level)

clear_k8s_deployments_by_names:
	./gradlew subprojects\:ci\:k8s-deployments-cleaner\:deleteByNames -Pci=true $(log_level)

# Clear local branches that not on remote
# from: https://stackoverflow.com/a/17029936/981330
unsafe_clear_local_branches:
	git fetch --prune && \
	git branch -r | awk '{print $$1}' | egrep -v -f /dev/fd/0 <(git branch -vv | grep origin) | \
	awk '{print $$1}' | xargs git branch -D

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
