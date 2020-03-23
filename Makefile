test_build_type?=debug

help:

publish_to_maven_local:
	./gradlew publishToMavenLocal --stacktrace -PprojectVersion=local

test_app_instrumentation_debug:
	./gradlew subprojects\:android-test\:test-app\:instrumentationUi -Dorg.gradle.jvmargs='-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=y' --no-daemon --stacktrace -PinfraVersion=local -Pci=true -Pavito.testBuildType=$(test_build_type)

test_app_instrumentation:
	./gradlew subprojects\:android-test\:test-app\:instrumentationUi --stacktrace -PinfraVersion=local -Pci=true -Pavito.testBuildType=$(test_build_type)

test_app_instrumentation_dynamic:
	./gradlew subprojects\:android-test\:test-app\:instrumentationDynamic --stacktrace -PinfraVersion=local -Pci=true -Pavito.testBuildType=$(test_build_type) -PdynamicTarget22=true

clear_k8s_deployments_by_namespaces:
	./gradlew subprojects\:ci\:k8s-deployments-cleaner\:clearByNamespaces -Pci=true

clear_k8s_deployments_by_names:
	./gradlew subprojects\:ci\:k8s-deployments-cleaner\:deleteByNames -Pci=true
