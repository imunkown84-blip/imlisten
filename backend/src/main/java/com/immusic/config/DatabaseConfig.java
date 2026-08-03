package com.immusic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url:#{null}}")
    private String configuredUrl;

    @Value("${spring.datasource.username:sa}")
    private String configuredUsername;

    @Value("${spring.datasource.password:}")
    private String configuredPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        String dbUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = System.getenv("DB_URL");
        }
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = System.getenv("DATABASE_URL");
        }
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = configuredUrl;
        }

        String username = System.getenv("DB_USERNAME");
        if (username == null || username.isBlank()) {
            username = System.getenv("SPRING_DATASOURCE_USERNAME");
        }
        if (username == null || username.isBlank()) {
            username = configuredUsername;
        }

        String password = System.getenv("DB_PASSWORD");
        if (password == null || password.isBlank()) {
            password = System.getenv("SPRING_DATASOURCE_PASSWORD");
        }
        if (password == null || password.isBlank()) {
            password = configuredPassword;
        }

        String driverClassName = "org.h2.Driver";

        if (dbUrl != null && !dbUrl.isBlank()) {
            if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
                try {
                    String rawUrl = dbUrl;
                    if (rawUrl.startsWith("postgres://")) {
                        rawUrl = "postgresql://" + rawUrl.substring("postgres://".length());
                    }
                    URI uri = new URI(rawUrl);
                    if (uri.getUserInfo() != null) {
                        String[] userInfo = uri.getUserInfo().split(":");
                        username = userInfo[0];
                        if (userInfo.length > 1) {
                            password = userInfo[1];
                        }
                    }
                    int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                    dbUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath();
                    driverClassName = "org.postgresql.Driver";
                } catch (Exception e) {
                    dbUrl = dbUrl.replace("postgres://", "jdbc:postgresql://")
                                 .replace("postgresql://", "jdbc:postgresql://");
                    driverClassName = "org.postgresql.Driver";
                }
            } else if (dbUrl.startsWith("jdbc:postgresql:")) {
                driverClassName = "org.postgresql.Driver";
            } else if (dbUrl.startsWith("jdbc:h2:")) {
                driverClassName = "org.h2.Driver";
            }
        } else {
            dbUrl = "jdbc:h2:mem:musiccatalog;DB_CLOSE_DELAY=-1";
            username = "sa";
            password = "";
            driverClassName = "org.h2.Driver";
        }

        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(dbUrl)
                .username(username)
                .password(password)
                .build();
    }
}
