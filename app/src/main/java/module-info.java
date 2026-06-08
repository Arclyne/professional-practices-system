module mx.uv.fei {

    requires etiquette.core;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires org.slf4j;
    requires statemachine.core;
    requires jakarta.mail;
    requires org.apache.pdfbox;
    requires org.apache.pdfbox.io;

    opens mx.uv.fei.appconfiguration ;

        opens mx.uv.fei.dataaccess.database ;
        opens mx.uv.fei.dataaccess.repositories;
        opens mx.uv.fei.domain.statemachine;
        opens mx.uv.fei.domain.statemachine.reducers ;
        opens mx.uv.fei.domain.statemachine.actions ;
        opens mx.uv.fei.domain.manager ;
        opens mx.uv.fei.presentation ;
        opens mx.uv.fei.presentation.components;

        requires java.desktop;
    requires java.net.http;

    exports mx.uv.fei.app;
    opens mx.uv.fei.domain.common;
    opens mx.uv.fei.domain.common.validators;
}