package com.musiccatalog.app.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url:}")
    private String configuredUrl;

    @Value("${spring.datasource.username:}")
    private String configuredUsername;

    @Value("${spring.datasource.password:}")
    private String configuredPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        // Check environment variables first (DB_URL, DATABASE_URL)
        String envUrl = System.getenv("DB_URL");
        if (!StringUtils.hasText(envUrl)) {
            envUrl = System.getenv("DATABASE_URL");
        }

        String rawUrl = StringUtils.hasText(envUrl) ? envUrl : configuredUrl;
        HikariConfig config = new HikariConfig();

        String parsedUsername = null;
        String parsedPassword = null;
        String jdbcUrl = rawUrl;

        // Convert Render postgres:// or postgresql:// to jdbc:postgresql://
        if (StringUtils.hasText(rawUrl) && (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://"))) {
            try {
                // Temporarily replace protocol with http for URI parsing
                String uriString = rawUrl.replaceFirst("^(postgres|postgresql)://", "http://");
                URI uri = new URI(uriString);

                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath(); // includes leading /

                jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;

                if (uri.getUserInfo() != null) {
                    String[] userInfo = uri.getUserInfo().split(":", 2);
                    parsedUsername = userInfo[0];
                    if (userInfo.length > 1) {
                        parsedPassword = userInfo[1];
                    }
                }
            } catch (Exception e) {
                // Fallback regex replacement if URI parsing fails
                jdbcUrl = rawUrl.replaceFirst("^(postgres|postgresql)://", "jdbc:postgresql://");
            }
        }

        config.setJdbcUrl(jdbcUrl);

        // Username precedence: explicit configured/env > URI parsed
        if (StringUtils.hasText(configuredUsername)) {
            config.setUsername(configuredUsername);
        } else if (StringUtils.hasText(parsedUsername)) {
            config.setUsername(parsedUsername);
        }

        // Password precedence: explicit configured/env > URI parsed
        if (StringUtils.hasText(configuredPassword)) {
            config.setPassword(configuredPassword);
        } else if (StringUtils.hasText(parsedPassword)) {
            config.setPassword(parsedPassword);
        }

        // Set driver class name
        if (jdbcUrl != null && jdbcUrl.startsWith("jdbc:h2:")) {
            config.setDriverClassName("org.h2.Driver");
        } else {
            config.setDriverClassName("org.postgresql.Driver");
        }

        return new HikariDataSource(config);
    }
}
