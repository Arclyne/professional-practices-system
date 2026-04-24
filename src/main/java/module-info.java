module mx.uv.fei {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive java.sql;

    opens mx.uv.fei to spring.core, spring.beans, spring.context;
    opens mx.uv.fei.config to spring.core, spring.beans, spring.context, spring.boot;
    opens mx.uv.fei.dataacces.database to spring.core, spring.beans, spring.context;
    opens mx.uv.fei.dataacces.repositories to spring.core, spring.beans, spring.context;

    opens mx.uv.fei.presentation to javafx.fxml;
    opens mx.uv.fei.presentation.components to javafx.fxml;

    exports mx.uv.fei;
    exports mx.uv.fei.dataacces.interfaces;
    exports mx.uv.fei.domain.dto;
    exports mx.uv.fei.dataacces.exceptions;
    exports mx.uv.fei.presentation.components;
}