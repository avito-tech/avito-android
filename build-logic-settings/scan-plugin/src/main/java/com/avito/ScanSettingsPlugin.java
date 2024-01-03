package com.avito;


import com.gradle.enterprise.gradleplugin.GradleEnterpriseExtension;

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;

import static com.avito.SettingsExt.booleanProperty;


public class ScanSettingsPlugin implements Plugin<Settings> {

    @Override
    public void apply(Settings settings) {
        settings.getPlugins().apply("com.gradle.enterprise");
        final var isCi = booleanProperty(settings, "ci", false);
        final var publishBuildScan = booleanProperty(settings, "avito.gradle.buildScan.publish", false);
        settings.getExtensions().configure(GradleEnterpriseExtension.class, gradleEnterprise -> {
            gradleEnterprise.buildScan(buildScan -> {
                buildScan.setTermsOfServiceUrl("https://gradle.com/terms-of-service");
                buildScan.setTermsOfServiceAgree("yes");
                buildScan.publishAlwaysIf(publishBuildScan);
                // Unstable in CI for unknown reasons
                // https://docs.gradle.com/enterprise/gradle-plugin/#failed_background_build_scan_uploads
                buildScan.setUploadInBackground(!isCi);
            });
        });
    }
}
