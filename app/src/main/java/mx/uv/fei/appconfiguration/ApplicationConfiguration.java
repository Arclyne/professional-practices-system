package mx.uv.fei.appconfiguration;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.config.annotation.etiquette.Provide;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;

public class ApplicationConfiguration implements IApplicationModule {

    private final IDatabaseConnection databaseConnection;
    private final String profile;

    public ApplicationConfiguration(IDatabaseConnection databaseConnection, String profile) {
        this.databaseConnection = databaseConnection;
        this.profile = profile;
    }

    @Provide
    public IDatabaseConnection databaseConnection() {
        return databaseConnection;
    }

    @Override
    public String retrieveGlobalProfile() {
        return profile;
    }
}