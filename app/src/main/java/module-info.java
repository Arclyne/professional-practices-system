module mx.uv.fei {

        requires etiquette.core;
        requires javafx.controls;
        requires javafx.fxml;
        requires java.sql;
        requires mysql.connector.j;

        opens mx.uv.fei.appconfiguration ;
        opens mx.uv.fei.dataacces.database ;


        opens mx.uv.fei.dataacces.repositories;


        opens mx.uv.fei.domain.statemachine;
        opens mx.uv.fei.domain.statemachine.reducers ;
        opens mx.uv.fei.domain.statemachine.actions ;
        opens mx.uv.fei.domain.manager ;


        opens mx.uv.fei.presentation ;
        opens mx.uv.fei.presentation.components;

        exports mx.uv.fei.app;
}