SHELL := /bin/bash

ANDROID_BUILDER_TAG=36883f8ae0
ifeq ($(origin DOCKER_REGISTRY),undefined)
    IMAGE_ANDROID_BUILDER=avitotech/android-builder:$(ANDROID_BUILDER_TAG)
else
    IMAGE_ANDROID_BUILDER=$(DOCKER_REGISTRY)/android/builder:$(ANDROID_BUILDER_TAG)
endif
IMAGE_DOCKER_IN_DOCKER=${DOCKER_REGISTRY}/android/docker-in-docker-image:c2ecce3a3e
GRADLE_HOME_DIR=$(HOME)/.gradle

# only need dependencies: https://docs.gradle.org/current/userguide/dependency_resolution.html#sub:ephemeral-ci-cache
GRADLE_CACHE_DIR=$(GRADLE_HOME_DIR)/caches/modules-2
GRADLE_WRAPPER_DIR=$(GRADLE_HOME_DIR)/wrapper
USER_ID=$(shell id -u $(USER))

docker=false
infra?=
ci?=false
log_level?=--quiet
kubernetesContext?=
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
	--volume "$(shell pwd)/ci/gradle.properties":/gradle/gradle.properties \
	--volume "$(GRADLE_CACHE_DIR)":/gradle/caches/modules-2 \
	--volume "$(GRADLE_WRAPPER_DIR)":/gradle/wrapper \
	--workdir /app \
	--env TZ="Europe/Moscow" \
	--env LOCAL_USER_ID="$(USER_ID)" \
	--env GRADLE_USER_HOME=/gradle \
	$(IMAGE_ANDROID_BUILDER)
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

params +=-Pci=$(ci)
params +=$(log_level)

ifdef kubernetesContext
params +=-PkubernetesContext=$(kubernetesContext)
else
params +=-PkubernetesUrl=$(KUBERNETES_URL)
params +=-PkubernetesNamespace=android-emulator
params +=-PkubernetesToken=$(KUBERNETES_TOKEN)
params +=-PkubernetesCaCertData=$(KUBERNETES_CA_CERT_DATA)
endif

ifeq ($(gradle_debug),true)
params +=-Dorg.gradle.debug=true --no-daemon
endif

ifeq ($(CONFIG_CACHE),true)
params +=--configuration-cache
endif

ifdef AVITO_REGISTRY
params +=-Pavito.registry=$(AVITO_REGISTRY)
endif

ifeq ($(dry_run),true)
params +=--dry-run
endif

ifdef stacktrace
params +=--stacktrace
endif

ifdef ARTIFACTORY_URL
params +=-PartifactoryUrl=$(ARTIFACTORY_URL)
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
      $(error Undefined argument: $1$(if $2, ($2))))

clean_build:
	rm -rf `find . -type d -name build`

clean_configuration_cache:
	rm -rf .gradle/configuration-cache

clean_build_cache:
	rm -rf ~/.gradle/caches/build-cache-1/

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

publish_to_maven_local:
	$(docker_command) ./gradlew --project-dir $(project) $(params) publishToMavenLocal -PprojectVersion=local --no-configuration-cache

stage_ui_tests:
	make publish_to_maven_local
	./gradlew --project-dir $(project) $(params) :android-test:ui-testing-core-app:instrumentationUi -DinfraVersion=local

# todo remove --no-daemon MBS-11385
# see https://avito-tech.github.io/avito-android/test_runner/SampleApp/
test_runner_instrumentation:
	$(docker_command) ./gradlew --project-dir samples $(params) :test-runner:instrumentationUi --no-daemon

compile:
	$(docker_command) ./gradlew $(params) compileAll

assemble:
	$(docker_command) ./gradlew $(params) assembleAll

# Configuration cache fails in instrumentation tasks: MBS-11856
check:
	$(docker_command) ./gradlew $(params) checkAll --no-configuration-cache

.PHONY: build
build:
	$(docker_command) ./gradlew $(params) build

# Analyze modules dependencies issues
# https://github.com/autonomousapps/dependency-analysis-android-gradle-plugin/wiki/Tasks#build-health
build_health:
	$(docker_command) ./gradlew --project-dir $(project) $(params) buildHealth

# Precondition: installed graphviz: https://graphviz.org/download/
#
# Builds dependencies graph
# https://github.com/autonomousapps/dependency-analysis-android-gradle-plugin
# Example: make project_graph_report id=:test-runner:client
project_graph_report:
	$(call check_defined, id)
	$(docker_command) ./gradlew --project-dir $(project) $(params) projectGraphReport --id $(id)
	cd $(project)/build/reports/dependency-analysis && \
		dot -Tsvg merged-graph.gv -o merged-graph.svg && \
		dot -Tsvg merged-graph-rev.gv -o merged-graph-rev.svg && \
		dot -Tsvg merged-graph-rev-sub.gv -o merged-graph-rev-sub.svg && \
		echo "See artifacts in $(project)/build/reports/dependency-analysis"

build_android_image:
	cd ./ci/docker/android-builder && \
	docker build -t android-builder .

# new ANDROID_BUILDER_TAG will be printed after successful publishing
internal_publish_android_builder:
	docker run --rm \
        --volume /var/run/docker.sock:/var/run/docker.sock \
        --volume "$(shell pwd)/ci/docker/android-builder":/build \
        --env DOCKER_REGISTRY=$(DOCKER_REGISTRY) \
        --env DOCKER_LOGIN=$(DOCKER_LOGIN) \
        --env DOCKER_PASSWORD=$(DOCKER_PASSWORD) \
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

dependency_updates:
	$(docker_command) ./gradlew --project-dir $(project) $(params) dependencyUpdates -Drevision=release

benchmark_fast_check:
	gradle-profiler --benchmark --project-dir subprojects --scenario-file gradle/performance.scenarios fastCheck

benchmark_gradle_test:
	gradle-profiler --benchmark --project-dir subprojects --scenario-file gradle/performance.scenarios gradleTest

# To generate new test fixtures for LintSlackAlertIntegrationTest
lint_test_app:
	$(docker_command) ./gradlew --project-dir $(project) $(params) :android-test:ui-testing-core-app:lint
	cp subprojects/android-test/ui-testing-core-app/build/reports/lint-results-debug.html subprojects/gradle/lint-report/src/integTest/resources/lint-results.html
	cp subprojects/android-test/ui-testing-core-app/build/reports/lint-results-debug.xml subprojects/gradle/lint-report/src/integTest/resources/lint-results.xml

## Gradle cache node
GRADLE_CACHE_NODE_TAG=9.9

# publish to internal repo to avoid rate limits problems
internal_publish_gradle_cache_node_image:
	$(call check_defined, DOCKER_REGISTRY)
	$(call check_defined, DOCKER_LOGIN)
	$(call check_defined, DOCKER_PASSWORD)
	echo $(DOCKER_PASSWORD) | docker login --username $(DOCKER_LOGIN) --password-stdin $(DOCKER_REGISTRY) && \
	docker pull gradle/build-cache-node:$(GRADLE_CACHE_NODE_TAG) && \
	docker tag gradle/build-cache-node:$(GRADLE_CACHE_NODE_TAG) $(DOCKER_REGISTRY)/android/gradle-cache-node:$(GRADLE_CACHE_NODE_TAG) && \
	docker push $(DOCKER_REGISTRY)/android/gradle-cache-node:$(GRADLE_CACHE_NODE_TAG)

deploy_gradle_cache_node:
	$(call check_defined, GRADLE_CACHE_NODE_HOST)
	cd ./ci/k8s/gradle-remote-cache && \
	sed -e 's|GRADLE_CACHE_NODE_HOST|$(GRADLE_CACHE_NODE_HOST)|g' -e 's|NODE_IMAGE|$(DOCKER_REGISTRY)/android/gradle-cache-node:$(GRADLE_CACHE_NODE_TAG)|g' github-project.yaml | kubectl apply -f - && \
	echo "Gradle Cache Node web interface should be available soon here: http://$(GRADLE_CACHE_NODE_HOST)"

delete_gradle_cache_node:
	cd ./ci/k8s/gradle-remote-cache && \
	kubectl delete -f github-project.yaml

deploy_avito_cache_node:
	$(call check_defined, AVITO_CACHE_NODE_HOST)
	cd ./ci/k8s/gradle-remote-cache && \
	sed -e 's|GRADLE_CACHE_NODE_HOST|$(AVITO_CACHE_NODE_HOST)|g' -e 's|NODE_IMAGE|$(DOCKER_REGISTRY)/android/gradle-cache-node:$(GRADLE_CACHE_NODE_TAG)|g' avito-project.yaml | kubectl apply -f - && \
	echo "Gradle Cache Node web interface should be available soon here: http://$(AVITO_CACHE_NODE_HOST)"

delete_avito_cache_node:
	cd ./ci/k8s/gradle-remote-cache && \
	kubectl delete -f avito-project.yaml

write_locks:
	./gradlew resolveAndLockAll --write-locks --no-configuration-cache --quiet > /dev/null
