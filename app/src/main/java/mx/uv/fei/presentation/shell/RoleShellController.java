package mx.uv.fei.presentation.shell;

import mx.uv.fei.config.annotation.core.DependencyInjector;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.presentation.interfaces.IDisposable;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

/**
 * Base reutilizable del panel por rol: barra lateral fija más un área de contenido
 * que intercambia sub-vistas sin recargar el shell. Cada rol la extiende y solo
 * declara sus accesos de navegación.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @version 1.0
 */
public abstract class RoleShellController implements IDisposable {

    private static final Logger log = LoggerFactory.getLogger(RoleShellController.class);

    private static final String ACTIVE_NAV_STYLE_CLASS = "sidebar-item-active";

    @FXML private StackPane shellContentArea;
    @FXML private Label shellUserNameLabel;
    @FXML private Label shellUserRoleLabel;

    private final AppStore store;
    private final DependencyInjector dependencyInjector;
    private final ShellNavigator shellNavigator;

    private Button activeNavButton;
    private Object currentSubViewController;

    protected RoleShellController(AppStore store, DependencyInjector dependencyInjector, ShellNavigator shellNavigator) {
        this.store = store;
        this.dependencyInjector = dependencyInjector;
        this.shellNavigator = shellNavigator;
    }

    @Override
    public void dispose() {
        disposeCurrentSubView();
    }

    protected void initializeShell() {
        shellNavigator.bind(this::loadSubViewIntoContent);
        populateUserHeader();
    }

    protected void showSubView(String fxmlPath) {
        shellNavigator.showSubView(fxmlPath);
    }

    private void loadSubViewIntoContent(String fxmlPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IllegalArgumentException("Ruta FXML no encontrada: " + fxmlPath);
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setControllerFactory(dependencyInjector::retrieveInstance);
            Parent subView = loader.load();

            disposeCurrentSubView();
            currentSubViewController = loader.getController();
            bindToContentArea(subView);
            shellContentArea.getChildren().setAll(subView);
        } catch (IOException e) {
            log.error("Error de E/S al cargar la sub-vista del panel: {}.", fxmlPath, e);
            Controller.showErrorAlert("Error de Interfaz", "No se pudo cargar la sección solicitada.");
        } catch (IllegalArgumentException e) {
            log.error("Ruta de sub-vista inválida: {}.", fxmlPath, e);
            Controller.showErrorAlert("Error de Interfaz", "La sección solicitada no existe en el sistema.");
        }
    }

    protected void selectNavButton(Button navButton) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove(ACTIVE_NAV_STYLE_CLASS);
        }
        if (navButton != null) {
            navButton.getStyleClass().add(ACTIVE_NAV_STYLE_CLASS);
            activeNavButton = navButton;
        }
    }

    protected void logout() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));
    }

    private void disposeCurrentSubView() {
        if (currentSubViewController instanceof IDisposable disposableController) {
            disposableController.dispose();
        }
        currentSubViewController = null;
    }

    private void populateUserHeader() {
        RootState currentState = store.getState();
        if (currentState != null && currentState.sessionState() != null) {
            User currentUser = currentState.sessionState().currentUserInSession();
            if (currentUser != null) {
                shellUserNameLabel.setText(currentUser.getName() + " " + currentUser.getLastName());
                shellUserRoleLabel.setText(currentUser.getRole());
            }
        }
    }

    private void bindToContentArea(Parent subView) {
        if (subView instanceof Region regionView) {
            regionView.prefWidthProperty().bind(shellContentArea.widthProperty());
            regionView.prefHeightProperty().bind(shellContentArea.heightProperty());
        }
    }
}
