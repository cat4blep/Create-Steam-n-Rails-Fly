/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.internal.compat;

import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateFlyVersionRangeTest {
    @Test
    void staysOnTheCompatibleCreateFly609Line() throws Exception {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(Path.of("gradle.properties"))) {
            properties.load(input);
        }

        VersionPredicate range = VersionPredicate.parse(
            properties.getProperty("create_fabric_version_range")
        );

        assertTrue(range.test(Version.parse("6.0.9-1")));
        assertTrue(range.test(Version.parse("6.0.9-2")));
        assertFalse(range.test(Version.parse("6.0.10-1")));
        assertFalse(range.test(Version.parse("6.0.10")));
    }
}
