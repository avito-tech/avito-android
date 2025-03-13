package com.avito;

import static com.avito.SettingsExt.booleanProperty;

import com.gradle.develocity.agent.gradle.DevelocityConfiguration;

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;

@SuppressWarnings("unused")
public class ScanSettingsPlugin implements Plugin<Settings> {

    @Override
    public void apply(Settings settings) {
        settings.getPlugins().apply("com.gradle.develocity");
        final var isCi = booleanProperty(settings, "ci", false);
        final var publishBuildScan = booleanProperty(settings, "avito.gradle.buildScan.publish", false);
        settings.getExtensions().configure(DevelocityConfiguration.class, develocity -> {
            develocity.buildScan(buildScan -> {
                buildScan.getTermsOfUseUrl().set("https://gradle.com/terms-of-service");
                buildScan.getTermsOfUseAgree().set("yes");
                buildScan.getPublishing().onlyIf(context -> publishBuildScan);
                // Unstable in CI for unknown reasons
                // https://docs.gradle.com/develocity/gradle-plugin/current/#failed_background_build_scan_uploads
                buildScan.getUploadInBackground().set(!isCi);
            });
        });
    }
}
