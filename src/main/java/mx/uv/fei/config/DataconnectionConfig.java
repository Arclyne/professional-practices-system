package mx.uv.fei.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import mx.uv.fei.dataacces.database.DatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;

@Configuration
@Profile("!test")
public class DataconnectionConfig {

    private DatabasePropeties propeties;

    public DataconnectionConfig(DatabasePropeties propeties) {
        this.propeties = propeties;
    }

    @Bean
    public IDatabaseConnection databaseConnection() {

        return new DatabaseConnection(propeties.getUrl(), propeties.getUser(), propeties.getPassword());
    }
}
