module mx.uv.fei {
        requires javafx.controls;
        requires javafx.fxml;
        requires transitive java.sql;
        requires etiquette.core;

        opens mx.uv.fei.app to etiquette.core;
        opens mx.uv.fei.appconfiguration to etiquette.core;
        opens mx.uv.fei.dataacces.database to etiquette.core;
        opens mx.uv.fei.dataacces.repositories to etiquette.core;
        opens mx.uv.fei.presentation to etiquette.core, javafx.fxml;

        exports mx.uv.fei.app to javafx.graphics;
        exports mx.uv.fei.presentation;

}