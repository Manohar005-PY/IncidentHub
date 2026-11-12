package com.manohar.incidenthub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.manohar.incidenthub.model.Incident;
import com.manohar.incidenthub.model.IncidentStatus;
import com.manohar.incidenthub.repositroy.IncidentRepositroy;
import com.manohar.incidenthub.service.IncidentService;

@Testcontainers
@SpringBootTest(properties = "INCIDENTHUB_DB_PASSWORD=test")
public class IncidentIntegrationTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private IncidentRepositroy incidentRepositroy;

    @Autowired
    private IncidentService incidentService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleandatabase() {
        incidentRepositroy.deleteAll();
    }

    @Test
    void shouldPersistIncidentAndAuditTimeStamps() {
        Incident created = incidentService.createIncident("payment service is unavilable", "HIGH");

        assertNotNull(created.getId());
        assertNotNull(created.getCreatedAt());
        assertNotNull(created.getUpdatedAt());

        Incident persisted = incidentRepositroy.findById(created.getId()).orElseThrow();

        assertEquals("payment service is unavilable", persisted.getTitle());
        assertEquals(IncidentStatus.OPEN, persisted.getStatus());
        assertNotNull(persisted.getCreatedAt());
        assertNotNull(persisted.getUpdatedAt());
    }

    @Test
    void shouldApplyFlywayMigrations() {

        Integer migrationCount = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '3'
                          AND success = TRUE
                        """,
                Integer.class);

        assertNotNull(migrationCount);
        assertEquals(1, migrationCount);
    }

    @Test
    void shouldRejectStaleIncidentUpdate() {
        Incident incident = incidentService.createIncident("payment service is unavilable", "HIGH");

        Incident adminA = incidentRepositroy.findById(incident.getId()).orElseThrow();
        Incident adminB = incidentRepositroy.findById(incident.getId()).orElseThrow();

        adminA.setTitle("updated by adminA");
        incidentRepositroy.saveAndFlush(adminA);
        adminB.setTitle("updated by adminB");

        assertThrows(OptimisticLockingFailureException.class, () -> incidentRepositroy.saveAndFlush(adminB));
        assertEquals("updated by adminA", adminA.getTitle());
    }
}
