package com.example.agent;

import dev.langchain4j.agent.tool.Tool;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class DatabaseTools {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseTools(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Tool("Inspects the database schema. Can provide details for a specific table or all tables if name is null.")
    public String schema_inspector(String tableName) {
        String query = "SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS";
        if (tableName != null && !tableName.isBlank()) {
            query += " WHERE TABLE_NAME = '" + tableName + "'";
        }
        
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query);
        return rows.stream()
                .map(row -> String.format("Table: %s, Column: %s, Type: %s", 
                        row.get("TABLE_NAME"), row.get("COLUMN_NAME"), row.get("DATA_TYPE")))
                .collect(Collectors.joining("\n"));
    }

    @Tool("Executes a safe SELECT SQL query and returns the results as JSON.")
    public List<Map<String, Object>> sql_query_executor(String sql) {
        // Enforce TOP 50 constraint as per README if not present
        String finalSql = sql;
        if (!sql.toUpperCase().contains("TOP")) {
            finalSql = sql.replaceFirst("(?i)SELECT", "SELECT TOP 50");
        }
        return jdbcTemplate.queryForList(finalSql);
    }

    @Tool("Validates if a SQL query is safe (READ-ONLY).")
    public String query_validator(String sql) {
        Pattern unsafePattern = Pattern.compile("(?i)\\b(DELETE|UPDATE|INSERT|DROP|ALTER|TRUNCATE)\\b");
        if (unsafePattern.matcher(sql).find()) {
            return "UNSAFE: Query contains restricted data-modifying keywords.";
        }
        return "SAFE";
    }
}
