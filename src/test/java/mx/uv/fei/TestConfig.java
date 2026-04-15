package mx.uv.fei;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;

@TestConfiguration(proxyBeanMethods = false)
public class TestConfig {

    @Bean
    @Primary
    public IDatabaseConnection databaseConnection(DataSource dataSource) {
        return () -> dataSource.getConnection();
    }
}
