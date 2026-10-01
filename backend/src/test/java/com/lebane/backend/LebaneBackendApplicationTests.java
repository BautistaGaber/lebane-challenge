package com.lebane.backend;

import com.lebane.backend.department.repository.DepartmentRepository;
import com.lebane.backend.storage.StorageService;
import com.lebane.backend.support.PostgresTestContainer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test proving the whole application context starts against the containerised PostgreSQL,
 * with Flyway migrations applied and the seed disabled.
 */
@SpringBootTest(properties = "app.seed.enabled=false")
@Transactional
@DisplayName("Application context")
class LebaneBackendApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        PostgresTestContainer.registerDatasource(registry);
    }

    @Test
    @DisplayName("loads with a real PostgreSQL database")
    void contextLoads() {
        // Assert
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.getBean(DepartmentRepository.class)).isNotNull();
        assertThat(applicationContext.getBean(StorageService.class)).isNotNull();
    }
}