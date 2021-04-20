package com.rbkmoney.midgard.test.integration;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import com.rbkmoney.midgard.MidgardClearingApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.KafkaContainer;

import javax.sql.DataSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.boot.test.util.TestPropertyValues.Type.MAP;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = {MidgardClearingApplication.class},
        initializers = AbstractIntegrationTest.Initializer.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class AbstractIntegrationTest {

    public static final String SOURCE_ID = "source_id";
    public static final String SOURCE_NS = "source_ns";
    private static final int PORT = 15432;
    private static final String dbName = "midgard";
    private static final String dbUser = "postgres";
    private static final String dbPassword = "postgres";
    private static final String jdbcUrl = "jdbc:postgresql://localhost:" + PORT + "/" + dbName;
    private static final String FILE_NAME = "target/test-classes/InsertFeedInitialData.sql";
    private static final String CONFLUENT_PLATFORM_VERSION = "5.0.1";
    @ClassRule
    public static KafkaContainer kafka = new KafkaContainer(CONFLUENT_PLATFORM_VERSION).withEmbeddedZookeeper();
    private static EmbeddedPostgres postgres;

    private static void startPgServer() {
        try {
            log.info("The PG server is starting...");
            EmbeddedPostgres.Builder builder = EmbeddedPostgres.builder();
            String dbDir = prepareDbDir();
            log.info("Dir for PG files: " + dbDir);
            builder.setDataDirectory(dbDir);
            builder.setPort(PORT);
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
            log.error("An error occurred while creating the database " + dbName, e);
            e.printStackTrace();
        }
    }

    private static String prepareDbDir() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentDate = dateFormat.format(new Date());
        String dir = "target" + File.separator + "pgdata_" + currentDate;
        log.info("Postgres source files in {}", dir);
        return dir;
    }

    @After
    public void destroy() throws IOException {
        if (postgres != null) {
            postgres.close();
            postgres = null;
        }
    }

    public void initDb() throws SQLException, IOException {
        try (Connection connection = getDataSource().getConnection()) {
            Statement statement = connection.createStatement();
            Path path = Paths.get(FILE_NAME);
            String insertScript = Files.readString(path);
            statement.execute(insertScript);
            statement.close();
            log.info("Initial data was inserted");
        } catch (IOException ex) {
            log.error("I/O exception while inserting data", ex);
            throw ex;
        } catch (SQLException ex) {
            log.error("Error while inserting data");
            throw ex;
        }
    }

    private DataSource getDataSource() {
        return postgres.getDatabase(dbUser, dbName);
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of("spring.datasource.url=" + jdbcUrl,
                    "spring.datasource.username=" + dbUser,
                    "spring.datasource.password=" + dbPassword,
                    "flyway.url=" + jdbcUrl,
                    "flyway.user=" + dbUser,
                    "flyway.password=" + dbPassword,
                    "clearing-service.adapters.bank.providerId=1",
                    "clearing-service.revision=100000",
                    "service.schedulator.registerEnable=false",

                    "kafka.bootstrap-servers=" + kafka.getBootstrapServers(),
                    "kafka.ssl.enabled=false",
                    "kafka.consumer.group-id=TestGroupId",
                    "kafka.consumer.enable-auto-commit=false",
                    "kafka.consumer.auto-offset-reset=earliest",
                    "kafka.topics.invoice.id=test-topic",
                    "kafka.topics.invoice.enabled=true",
                    "clearing-service.adapters.[0].name=mock_1",
                    "clearing-service.adapters.[0].url=http://localhost:8023/v1/adapter/mock_1",
                    "clearing-service.adapters.[0].networkTimeout=60000",
                    "clearing-service.adapters.[0].providerId=1",
                    "clearing-service.adapters.[0].package-size=25",
                    "clearing-service.adapters.[1].name=test_bank",
                    "clearing-service.adapters.[1].scheduler.jobId=midgardClearingJobMock",
                    "clearing-service.adapters.[1].scheduler.revisionId=16396",
                    "clearing-service.adapters.[1].scheduler.schedulerId=64",
                    "clearing-service.adapters.[1].scheduler.calendarId=1",
                    "clearing-service.adapters.[1].scheduler.serviceCallbackPath=" +
                            "http://midgard:8022/v1/clearing_scheduler_job",
                    "clearing-service.adapters.[1].url=http://localhost:8023/v1/adapter/mock_2",
                    "clearing-service.adapters.[1].networkTimeout=60000",
                    "clearing-service.adapters.[1].package-size=25",
                    "clearing-service.adapters.[1].providerId=115")
                    .applyTo(configurableApplicationContext.getEnvironment(), MAP, "testcontainers");

            if (postgres == null) {
                startPgServer();
                createDatabase();
            }
        }

    }

}