SHELL := /bin/bash

ANDROID_BUILDER_TAG=eddf45adfa
ifeq ($(origin DOCKER_REGISTRY),undefined)
    IMAGE_ANDROID_BUILDER=avitotech/android-builder:$(ANDROID_BUILDER_TAG)
else
    IMAGE_ANDROID_BUILDER=$(DOCKER_REGISTRY)/android/builder:$(ANDROID_BUILDER_TAG)
endif
IMAGE_DOCKER_IN_DOCKER=${DOCKER_REGISTRY}/android/docker-in-docker-image:c2ecce3a3e
GRADLE_HOME_DIR=$(HOME)/.gradle
GRADLE_CACHE_DIR=$(GRADLE_HOME_DIR)/caches
GRADLE_WRAPPER_DIR=$(GRADLE_HOME_DIR)/wrapper
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
dry_run=false
instrumentation=Ui
stacktrace?=
project=subprojects

# see Logging.md#Verbose-mode
verbose?=

docker_command?=

ifeq ($(docker),true)
define docker_command
make clear_gradle_lock_files
make clear_docker_containers
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

ifdef verbose
params +=-Pavito.logging.verbosity=$(verbose)
endif

params +=-PtestBuildType=$(test_build_type)
params +=-Pci=$(ci)
params +=$(log_level)
params +=-PkubernetesContext=$(kubernetesContext)

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
      $(error Undefined $1$(if $2, ($2)) argument))

clean:
	rm -rf `find . -type d -name build`

unsafe_clean:
	git clean -fdx

# Warning. Hack!
# Мы можем удалять эти локи, т.к. гарантированно никакие другие процессы не используют этот шаренный кеш на начало новой сборки
# см. clearDockerContainers
# То что лок файлы остаются от предыдущих сборок, означает что мы где-то неправильно останавливаем процесс
# '|| true' необходим для свеже-поднятых агентов, где еще не создана папка с кешами
clear_gradle_lock_files:
	find "$(GRADLE_HOME_DIR)" \( -name "*.lock" -o -name "*.lck" \) -delete || true

# По-разным причинам работа контейнера при прошлой сборке может не завершиться
# Здесь мы перестраховываемся и останавливаем все работающие контейнеры
# Перед сборкой не должно быть других контейнеров в любом случае
clear_docker_containers:
	containers=$(docker container ls -aq)
	if [[ ! -z "$(containers)" ]]; then \
	docker container rm --force $(containers); \
	fi

help:
	$(docker_command) ./gradlew -p $(project) $(log_level) $(params) help

publish_to_maven_local:
	$(docker_command) ./gradlew -p $(project) $(log_level) $(params) publishToMavenLocal -PprojectVersion=local

stage_ui_tests:
	make publish_to_maven_local
	./gradlew -p $(project) $(log_level) $(params) :android-test:ui-testing-core-app:instrumentationUi -DinfraVersion=local

test_runner_instrumentation:
	./gradlew -p samples $(log_level) $(params) :test-runner:instrumentationUi

unit_tests:
	$(docker_command) ./gradlew -p $(project) $(log_level) $(params) test

gradle_test:
	$(docker_command) ./gradlew -p $(project) $(log_level) $(params) gradleTest

integration_tests:
	$(docker_command) ./gradlew -p $(project) $(log_level) $(params) integrationTest

compile_tests:
	$(docker_command) ./gradlew -p $(project) $(log_level) $(params) compileTestKotlin

compile:
	$(docker_command) ./gradlew -p $(project) $(log_level) $(params) compileAll

check:
	$(docker_command) ./gradlew -p $(project) $(log_level) $(params) check

.PHONY: build
build:
	$(docker_command) ./gradlew -p $(project) $(log_level) $(params) build

fast_check:
	$(docker_command) ./gradlew -p $(project) $(log_level) $(params) compileAll detektAll test

full_check:
	$(docker_command) ./gradlew -p $(project) $(log_level) $(params) compileAll detektAll test gradleTest

clean_fast_check:
	make clean
	$(docker_command) ./gradlew -p $(project) $(log_level) $(params) compileAll detektAll test --rerun-tasks --no-build-cache

detekt:
	$(docker_command) ./gradlew -p $(project) $(log_level) $(params) detektAll

# Analyze modules dependencies issues
# https://github.com/autonomousapps/dependency-analysis-android-gradle-plugin/wiki/Tasks#build-health
build_health:
	$(docker_command) ./gradlew -p $(project) $(log_level) $(params) buildHealth

# Builds dependencies graph
# https://github.com/autonomousapps/dependency-analysis-android-gradle-plugin
# Example: make project_graph_report id=:test-runner:client
project_graph_report:
	$(call check_defined, id)
	$(docker_command) ./gradlew -p $(project) $(log_level) $(params) projectGraphReport --id $(id) --rerun-tasks
	cd $(project)/build/reports/dependency-analysis && \
		dot -Tsvg merged-graph.gv -o merged-graph.svg && \
		dot -Tsvg merged-graph-rev.gv -o merged-graph-rev.svg && \
		dot -Tsvg merged-graph-rev-sub.gv -o merged-graph-rev-sub.svg

build_android_image:
	cd ./ci/docker/android-builder && \
	docker build -t android-builder .

# new ANDROID_BUILDER_TAG will be printed after successful publishing
internal_publish_android_builder:
	docker run --rm \
        --volume /var/run/docker.sock:/var/run/docker.sock \
        --volume "$(shell pwd)/ci/docker/android-builder":/build \
        --env DOCKER_REGISTRY=${DOCKER_REGISTRY} \
        --env DOCKER_LOGIN=${DOCKER_LOGIN} \
        --env DOCKER_PASSWORD=${DOCKER_PASSWORD} \
        ${IMAGE_DOCKER_IN_DOCKER} publish_docker_image publish /build

# run after new ANDROID_BUILDER_TAG set
# assume `docker login` to dockerhub was successful
publish_android_builder:
	docker tag $(ANDROID_BUILDER_TAG) avitotech/android-builder:$(ANDROID_BUILDER_TAG)
	docker push avitotech/android-builder:$(ANDROID_BUILDER_TAG)

.PHONY: docs
docs:
	./ci/documentation/lint.sh
	./ci/documentation/preview.sh

clear_k8s_deployments_by_namespaces:
	$(docker_command) ./gradlew subprojects\:test-runner\:k8s-deployments-cleaner\:clearByNamespaces -PteamcityApiPassword=$(teamcityApiPassword) $(log_level)

clear_k8s_deployments_by_names:
	$(docker_command) ./gradlew subprojects\:test-runner\:k8s-deployments-cleaner\:deleteByNames -Pci=true $(log_level)

# Clear local branches that not on remote
# from: https://stackoverflow.com/a/17029936/981330
unsafe_clear_local_branches:
	git fetch --prune && \
	git branch -r | awk '{print $$1}' | egrep -v -f /dev/fd/0 <(git branch -vv | grep origin) | \
	awk '{print $$1}' | xargs git branch -D

# Precondition:
# - installed CLI: https://cli.github.com/
# - push $(version) branch
#
# Post actions:
# - Go to a link in output
# - Edit notes
# - Publish the release
draft_release:
	$(call check_defined, version)
	$(call check_defined, prev_version)
	git fetch --all
	gh release create $(version) \
		--draft \
		--target $(version) \
		--title $(version) \
		--notes "$$(git log --pretty=format:%s origin/$(prev_version)..origin/$(version) | cat)"

dynamic_properties:
	$(eval keepFailedTestsFromReport?=)
	$(eval skipSucceedTestsFromPreviousRun=true)
	$(eval testFilter?=empty)
	$(eval dynamicPrefixFilter?=)

check_avito_configuration:
	make publish_to_maven_local
	cd ../avito-android && ./gradlew tasks -DinfraVersion=local
