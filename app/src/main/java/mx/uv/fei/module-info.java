module mx.uv.fei {
        requires javafx.controls;
        requires javafx.fxml;
        requires transitive java.sql;

        requires etiquette.core;

        // Abre tus paquetes para que el inyector pueda leer campos privados
        opens mx.uv.fei.dataacces.database to etiquette.core;
        opens mx.uv.fei.config to etiquette.core;

        // Exporta para que JavaFX vea tus controladores
        exports mx.uv.fei.presentation;
}