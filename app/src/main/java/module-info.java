module mx.uv.fei {

        requires etiquette.core;
        requires javafx.controls;
        requires javafx.fxml;
        requires java.sql;
        requires mysql.connector.j;

        opens mx.uv.fei.appconfiguration to etiquette.core;
        opens mx.uv.fei.dataacces.database to etiquette.core , org.junit.platform.commons;


        opens mx.uv.fei.dataacces.repositories to etiquette.core , org.junit.platform.commons ;


        opens mx.uv.fei.domain.statemachine to etiquette.core;
        opens mx.uv.fei.domain.statemachine.reducers to etiquette.core;
        opens mx.uv.fei.domain.statemachine.actions to etiquette.core;
        opens mx.uv.fei.domain.manager to etiquette.core;


        opens mx.uv.fei.presentation to javafx.fxml, etiquette.core;
        opens mx.uv.fei.presentation.components to javafx.fxml, etiquette.core;

        exports mx.uv.fei.app;
}