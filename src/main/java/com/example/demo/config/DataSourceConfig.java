package com.example.demo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Intelligent DataSource configuration that provides seamless compatibility:
 * 1. Automatically connects to MySQL when a reachable host (Docker Compose 'mysql', K8s 'mysql-service',
 *    localhost, or remote cloud database) is available.
 * 2. Gracefully falls back to an embedded MySQL-compatible H2 database when MySQL is not reachable,
 *    enabling zero-configuration cloud deployment on platforms like Render, Railway, or Heroku without crashing!
 */
@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    private static final Pattern JDBC_HOST_PORT_PATTERN = Pattern.compile("jdbc:mysql://([^:/]+)(?::(\\d+))?(/.*)?");

    @Value("${spring.datasource.url:}")
    private String configuredUrl;

    @Value("${spring.datasource.username:root}")
    private String configuredUsername;

    @Value("${spring.datasource.password:anits}")
    private String configuredPassword;

    @Value("${spring.datasource.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String configuredDriver;

    @Bean
    @Primary
    public DataSource dataSource() {
        // If explicitly pointed to H2, use H2 directly
        if (configuredUrl != null && configuredUrl.startsWith("jdbc:h2:")) {
            log.info("Datasource explicitly configured for H2 database.");
            return createH2DataSource(configuredUrl, configuredUsername, configuredPassword);
        }

        // Test reachability of configured MySQL host
        HostPort target = extractHostAndPort(configuredUrl);
        boolean reachable = false;

        if (target != null && target.host != null && !target.host.trim().isEmpty()) {
            reachable = testSocketConnection(target.host, target.port, 2000);
        }

        if (reachable) {
            log.info("Successfully established connection to MySQL host at {}:{}. Activating production MySQL DataSource.", target.host, target.port);
            try {
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(configuredUrl);
                config.setUsername(configuredUsername);
                config.setPassword(configuredPassword);
                config.setDriverClassName(configuredDriver);
                config.setMaximumPoolSize(10);
                config.setConnectionTimeout(5000);
                config.setValidationTimeout(3000);
                return new HikariDataSource(config);
            } catch (Exception e) {
                log.warn("Failed to initialize Hikari pool for MySQL: {}. Falling back to embedded H2.", e.getMessage());
            }
        } else {
            String targetDesc = (target != null) ? (target.host + ":" + target.port) : configuredUrl;
            log.warn("===============================================================================");
            log.warn("⚠️ MySQL host [{}] is not reachable or host name could not be resolved.", targetDesc);
            log.warn("🚀 Activating embedded MySQL-compatible H2 database for smooth cloud deployment (Render/Demo).");
            log.warn("===============================================================================");
        }

        return createH2DataSource(
                "jdbc:h2:mem:student_management;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
                "sa",
                ""
        );
    }

    private DataSource createH2DataSource(String url, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username != null && !username.isEmpty() ? username : "sa");
        config.setPassword(password != null ? password : "");
        config.setDriverClassName("org.h2.Driver");
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }

    private HostPort extractHostAndPort(String url) {
        if (url == null || url.trim().isEmpty()) return null;
        Matcher matcher = JDBC_HOST_PORT_PATTERN.matcher(url);
        if (matcher.find()) {
            String host = matcher.group(1);
            int port = 3306;
            String portStr = matcher.group(2);
            if (portStr != null && !portStr.isEmpty()) {
                try {
                    port = Integer.parseInt(portStr);
                } catch (NumberFormatException ignored) {}
            }
            return new HostPort(host, port);
        }
        return null;
    }

    private boolean testSocketConnection(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            log.debug("MySQL connection test for {}:{} failed: {}", host, port, e.getMessage());
            return false;
        }
    }

    private static class HostPort {
        final String host;
        final int port;
        HostPort(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }
}
