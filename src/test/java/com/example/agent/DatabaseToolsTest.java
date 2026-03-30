package com.example.agent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatabaseToolsTest {

    private DatabaseTools databaseTools;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setup() {
        jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        databaseTools = new DatabaseTools(jdbcTemplate);
    }

    @Test
    public void testQueryValidatorSafe() {
        String sql = "SELECT * FROM customers";
        String result = databaseTools.query_validator(sql);
        assertEquals("SAFE", result);
    }

    @Test
    public void testQueryValidatorUnsafe() {
        String sql = "DELETE FROM customers";
        String result = databaseTools.query_validator(sql);
        assertEquals("UNSAFE: Query contains restricted data-modifying keywords.", result);
    }

    @Test
    public void testQueryValidatorUnsafeCaseInsensitive() {
        String sql = "drop table main";
        String result = databaseTools.query_validator(sql);
        assertEquals("UNSAFE: Query contains restricted data-modifying keywords.", result);
    }
}
