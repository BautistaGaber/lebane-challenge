package com.lebane.backend.support;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Single PostgreSQL instance shared by the whole test JVM.
 *
 * <p>Starting one container per test class is the usual Testcontainers example, but it dominates
 * the runtime of this suite. The instance is created lazily in a static initializer and reused, so
 * Flyway runs once per JVM and every suite observes an identical schema.
 */
public final class PostgresTestContainer {

    private static final DockerImageName IMAGE = DockerImageName.parse("postgres:16-alpine");

    private static final PostgreSQLContainer<?> CONTAINER = new PostgreSQLContainer<>(IMAGE);

    static {
        CONTAINER.start();
    }

    private PostgresTestContainer() {
    }

    public static PostgreSQLContainer<?> getInstance() {
        return CONTAINER;
    }

    /**
     * Points the Spring datasource at the container. Called from the {@code @DynamicPropertySource}
     * of every integration test so no developer-local database is ever involved.
     */
    public static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", CONTAINER::getUsername);
        registry.add("spring.datasource.password", CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", CONTAINER::getDriverClassName);
    }
}