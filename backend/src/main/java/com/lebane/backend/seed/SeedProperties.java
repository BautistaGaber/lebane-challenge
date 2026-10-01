package com.lebane.backend.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.seed")
public record SeedProperties(boolean enabled, int departmentCount) { }
