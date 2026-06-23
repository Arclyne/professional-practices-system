package mx.uv.fei.presentation.shell;

import mx.uv.fei.config.annotation.etiquette.Component;

import java.util.function.Consumer;

@Component
public class ShellNavigator {

    private Consumer<String> subViewLoader;
    private String currentViewPath;
    private String returnViewPath;
    private Object pendingEntity;

    public void bind(Consumer<String> subViewLoader) {
        this.subViewLoader = subViewLoader;
    }

    public void showSubView(String fxmlPath) {
        currentViewPath = fxmlPath;
        loadView(fxmlPath);
    }

    public void openForm(String formPath) {
        openForm(formPath, null);
    }

    public void openForm(String formPath, Object entity) {
        returnViewPath = currentViewPath;
        pendingEntity = entity;
        loadView(formPath);
    }

    public Object consumePendingEntity() {
        Object entity = pendingEntity;
        pendingEntity = null;
        return entity;
    }

    public void returnToList() {
        if (returnViewPath != null) {
            showSubView(returnViewPath);
        }
    }

    private void loadView(String fxmlPath) {
        if (subViewLoader != null) {
            subViewLoader.accept(fxmlPath);
        }
    }
}
