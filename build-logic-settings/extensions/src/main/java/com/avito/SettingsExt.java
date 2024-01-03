package com.avito;

import org.gradle.api.initialization.Settings;
import org.gradle.api.plugins.ExtraPropertiesExtension;

import java.util.Objects;

public class SettingsExt {
    static boolean booleanProperty(Settings settings, String name, boolean defaultValue) {
        ExtraPropertiesExtension extra = settings.getExtensions().getExtraProperties();
        if (extra.has(name)) {
            return Boolean.parseBoolean(Objects.requireNonNull(extra.get(name)).toString());
        } else {
            return defaultValue;
        }
    }
}
