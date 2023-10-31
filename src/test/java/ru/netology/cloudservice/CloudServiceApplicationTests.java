package ru.netology.cloudservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;




@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {CloudServiceApplicationTests.Initializer.class})
public class CloudServiceApplicationTests {

    @Autowired
    TestRestTemplate restTemplate;

    private static final int PORT_DB = 5434;

    private static final String DATABASE_NAME = "postgres";
    private static final String DATABASE_USERNAME = "postgres";
    private static final String DATABASE_PASSWORD = "postgres";

    private final static Network CLOUD_NETWORK = Network.newNetwork();

    @Container
    public static PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres")
            .withNetwork(CLOUD_NETWORK)
            .withExposedPorts(PORT_DB)
            .withDatabaseName(DATABASE_NAME)
            .withUsername(DATABASE_USERNAME)
            .withPassword(DATABASE_PASSWORD);

    @Test
    void contextLoadsPostgres_test() {
        var portDatabase = POSTGRES.getMappedPort(PORT_DB);
        System.out.println(POSTGRES.getJdbcUrl() + " " + POSTGRES.getDatabaseName() + " " + POSTGRES.getPassword());
        System.out.println("Network ID: " + CLOUD_NETWORK.getId());
        System.out.println("CloudDrive Database -> port: " + portDatabase);
        Assertions.assertTrue(POSTGRES.isRunning());
    }


    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
                    "spring.datasource.username=" + POSTGRES.getUsername(),
                    "spring.datasource.password=" + POSTGRES.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

}
