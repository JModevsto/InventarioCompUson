package mx.unison;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.event.ActionEvent;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

public class InicioController {

    // 1. INYECCIÓN DEL ÁREA DE CONTENIDO (IMPORTANTE para navegación interna)
    @FXML
    private AnchorPane contentArea; 

    // --- MÉTODOS DE ACCIÓN PRINCIPAL ---
    
    // Botón "Inicio (Cerrar Sesión)"
    @FXML
    private void handleLogout(ActionEvent event) throws IOException {
        // Regresa a la ventana de Login
        mx.unison.InventarioApp.mostrarVista("Login.fxml");
    }

    // Botón "Productos"
    @FXML
    private void handleLoadProductos(ActionEvent event) {
        // Carga la vista Productos.fxml en el centro
        try {
        // Asumiendo que InventarioApp.mostrarVista(String fxml) usa .getResource(fxml)
        mx.unison.InventarioApp.mostrarVista("Productos.fxml");
    } catch (IOException e) {
        System.err.println("Error al cargar la vista interna: Productos.fxml");
        e.printStackTrace(); // <-- Es crucial ver el stack trace completo si esto no funciona
    }
    }

    // Botón "Almacenes"
    @FXML
    private void handleLoadAlmacenes(ActionEvent event) {
        // Carga la vista Almacenes.fxml en el centro
        loadContent("Almacenes.fxml");
    }

    // --- MÉTODO DE NAVEGACIÓN INTERNA ---
    
    // Método genérico para cargar cualquier FXML en el 'contentArea'
    private void loadContent(String fxmlFile) {
        try {
            Parent content = FXMLLoader.load(getClass().getResource(fxmlFile));
            
            // Limpiar el contenido anterior y establecer el nuevo
            contentArea.getChildren().setAll(content);
            
            // Ajustar el nuevo contenido para que ocupe todo el espacio
            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);
            
        } catch (IOException e) {
            System.err.println("Error al cargar la vista interna: " + fxmlFile);
            // Puedes usar tu popup de error aquí si lo deseas
        }
    }
}