test_build_type?=debug

help:

publish_to_maven_local:
	./gradlew publishToMavenLocal --stacktrace -PprojectVersion=local

test_app_instrumentation_debug:
	./gradlew subprojects\:android-test\:test-app\:instrumentationUi -Dorg.gradle.jvmargs='-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=y' --no-daemon --stacktrace -PinfraVersion=local -Pci=true -PtestBuildType=$(test_build_type)

test_app_instrumentation:
	./gradlew subprojects\:android-test\:test-app\:instrumentationUi --stacktrace -PinfraVersion=local -Pci=true -PtestBuildType=$(test_build_type)

dynamic_properties:
	$(eval testFilter?=empty)

test_app_instrumentation_dynamic: dynamic_properties
	./gradlew subprojects\:android-test\:test-app\:instrumentationDynamic --stacktrace -PinfraVersion=local -Pci=true -PtestBuildType=$(test_build_type) -PdynamicTarget22=true -Pinstrumentation.dynamic.testFilter=$(testFilter)

clear_k8s_deployments_by_namespaces:
	./gradlew subprojects\:ci\:k8s-deployments-cleaner\:clearByNamespaces -Pci=true

clear_k8s_deployments_by_names:
	./gradlew subprojects\:ci\:k8s-deployments-cleaner\:deleteByNames -Pci=true
