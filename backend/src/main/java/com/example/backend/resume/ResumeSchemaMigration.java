package com.example.backend.resume;

import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class ResumeSchemaMigration {
    @Bean
    ApplicationRunner widenResumeContentColumn(JdbcTemplate jdbc) {
        return args -> {
            List<String> dataTypes = jdbc.query(
                    "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_resumes' AND COLUMN_NAME = 'content'",
                    (result, row) -> result.getString(1));
            if (!dataTypes.isEmpty() && !"longtext".equalsIgnoreCase(dataTypes.get(0))) {
                jdbc.execute("ALTER TABLE user_resumes MODIFY COLUMN content LONGTEXT NOT NULL");
            }
        };
    }
}