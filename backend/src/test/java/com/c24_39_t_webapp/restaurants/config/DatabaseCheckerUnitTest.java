package com.c24_39_t_webapp.restaurants.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class DatabaseCheckerUnitTest {

    @Test
    @DisplayName("Cuando BD conecta → Imprime mensaje de éxito")
    void whenDatabaseConnects_thenPrintSuccess() throws Exception {
        // Arrange
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM information_schema.tables", Integer.class))
                .thenReturn(15);

        DatabaseChecker checker = new DatabaseChecker(jdbcTemplate);

        // Act & Assert - No debe lanzar excepción
        assertDoesNotThrow(() -> checker.run());
    }

    @Test
    @DisplayName("Cuando BD falla → Captura excepción sin relanzar")
    void whenDatabaseFails_thenCatchesException() throws Exception {
        // Arrange
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenThrow(new DataAccessException("Connection timeout") {});

        DatabaseChecker checker = new DatabaseChecker(jdbcTemplate);

        // Act & Assert - No debe lanzar excepción (se captura en catch)
        assertDoesNotThrow(() -> checker.run());
    }
}