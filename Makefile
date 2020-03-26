test_build_type?=debug
log_level?=-q
filter_report_id?=

help:

publish_to_maven_local:
	./gradlew publishToMavenLocal -PprojectVersion=local $(log_level)

test_app_instrumentation_debug:
	./gradlew subprojects\:android-test\:test-app\:instrumentationUi -Dorg.gradle.jvmargs='-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=y' --no-daemon -PinfraVersion=local -Pci=true -PtestBuildType=$(test_build_type) $(log_level)

test_app_instrumentation:
	./gradlew subprojects\:android-test\:test-app\:instrumentationUi -PinfraVersion=local -Pci=true -PtestBuildType=$(test_build_type) $(log_level)

dynamic_properties:
	$(eval filter_report_id?=)
	$(eval filter_already_succeed=true)
	$(eval testFilter?=empty)

test_app_instrumentation_dynamic: dynamic_properties
	./gradlew subprojects\:android-test\:test-app\:instrumentationDynamic -PinfraVersion=local -Pci=true -PtestBuildType=$(test_build_type) -PdynamicTarget22=true -Pinstrumentation.dynamic.testFilter=$(testFilter) -Pinstrumentation.dynamic.filterSucceedAndNewByReport=$(filter_report_id) -Pinstrumentation.dynamic.filterSucceedTestsByPreviousRun=$(filter_already_succeed) $(log_level)

unit_tests:
	./gradlew test $(log_level)

clear_k8s_deployments_by_namespaces:
	./gradlew subprojects\:ci\:k8s-deployments-cleaner\:clearByNamespaces -Pci=true $(log_level)

clear_k8s_deployments_by_names:
	./gradlew subprojects\:ci\:k8s-deployments-cleaner\:deleteByNames -Pci=true $(log_level)
