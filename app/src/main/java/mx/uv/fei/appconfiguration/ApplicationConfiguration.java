package mx.uv.fei.appconfiguration;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;

public class ApplicationConfiguration implements IApplicationModule {
    private final String profile;

    public ApplicationConfiguration(String profile) {
        this.profile = profile;
    }

    @Override
    public String retrieveGlobalProfile() {
        return profile;
    }
}