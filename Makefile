help:

publish_to_maven_local:
	./gradlew publishToMavenLocal --stacktrace -PprojectVersion=local

test_app_instrumentation_debug:
	./gradlew subprojects\:android-test\:test-app\:instrumentationUi -Dorg.gradle.jvmargs='-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=5005,suspend=y' --no-daemon --stacktrace -PinfraVersion=local -Pci=true

test_app_instrumentation:
	./gradlew subprojects\:android-test\:test-app\:instrumentationUi --stacktrace -PinfraVersion=local -Pci=true