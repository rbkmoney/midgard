package com.rbkmoney.midgard.base.tests.integration;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import com.rbkmoney.midgard.service.MidgardClearingApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.boot.test.util.TestPropertyValues.Type.MAP;

/**
 * Created by jeckep on 08.02.17.
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = {"bm.pollingEnabled=false"})
@ContextConfiguration(classes = {MidgardClearingApplication.class},
        initializers = AbstractIntegrationTest.Initializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractIntegrationTest {

    private static final String projectBuildDir = "target";

    private static final int port = 15432;

    private static final String dbName = "midgard";

    private static final String dbUser = "postgres";

    private static final String dbPassword = "postgres";

    private static final String jdbcUrl = "jdbc:postgresql://localhost:" + port + "/" + dbName;

    public static EmbeddedPostgres postgres;

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            if (postgres == null) {
                startPgServer();
                createDatabase();
            }

            TestPropertyValues.of("spring.datasource.url=" + jdbcUrl,
                    "spring.datasource.username=" + dbUser,
                    "spring.datasource.password=" + dbPassword,
                    "flyway.url=" + jdbcUrl,
                    "flyway.user=" + dbUser,
                    "flyway.password=" + dbPassword)
                    .applyTo(configurableApplicationContext.getEnvironment(), MAP, "testcontainers");

        }

    }

    private static void startPgServer() {
        try {
            log.info("The PG server is starting...");
            EmbeddedPostgres.Builder builder = EmbeddedPostgres.builder();
            String dbDir = prepareDbDir(projectBuildDir);
            log.info("Dir for PG files: " + dbDir);
            builder.setDataDirectory(dbDir);
            builder.setPort(port);
            postgres = builder.start();
            log.info("The PG server was started!");
        } catch (IOException e) {
            log.error("An error occurred while starting server ", e);
            e.printStackTrace();
        }
    }

    private static void createDatabase() {
        try (Connection conn = postgres.getPostgresDatabase().getConnection()) {
            Statement statement = conn.createStatement();
            statement.execute("CREATE DATABASE " + dbName);
            statement.close();
        } catch (SQLException e) {
            log.error("An error occurred while creating the database "+ dbName, e);
            e.printStackTrace();
        }
    }

    private static void dropScheme(String schemaName) {
        log.debug("Delete {} scheme", schemaName);
        DataSource database = postgres.getDatabase(dbUser, dbName);
        try (Connection connection = database.getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("DROP SCHEMA " + schemaName + " CASCADE");
            statement.close();
        } catch (SQLException ex) {
            log.error("An error occurred while drop the schema " + schemaName);
            ex.printStackTrace();
        }
    }

    private static String prepareDbDir(String projectBuildDir) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentDate = dateFormat.format(new Date());
        return projectBuildDir + File.separator + "pgdata_" + currentDate;
    }

    public static void destroy() throws IOException {
        if (postgres != null) {
            postgres.close();
            postgres = null;
        }
    }

}