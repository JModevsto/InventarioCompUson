package mx.unison;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.SQLException;

// IMPORTANTE: Este controlador fue adaptado para coincidir con los campos ID y NOMBRE de su FXML
public class AlmacenesFormController {

    // --- Inyecciones FXML ---
    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    // --- Variables de Estado ---
    private Almacen almacenEnEdicion; // Será null para "Agregar"
    private boolean esModificacion = false;

    // ------------------------------------------------------------------
    // 			MÉTODOS DE INICIALIZACIÓN
    // ------------------------------------------------------------------
    
    @FXML
    public void initialize() {
        // El ID no debe ser modificable en la mayoría de los casos
    }

    /**
     * Método llamado por InventarioApp para preparar el formulario para edición o agregar.
     * @param almacen El Almacen a cargar, o null si es modo "Agregar".
     */
    public void setAlmacen(Almacen almacen) {
        this.almacenEnEdicion = almacen;
        
        if (almacenEnEdicion != null) {
            // Modo MODIFICAR
            esModificacion = true;
            lblTitulo.setText("Modificar Almacén ID: " + almacen.getId());
            btnGuardar.setText("Actualizar");
            
        } else {
            // Modo AGREGAR
            esModificacion = false;
            lblTitulo.setText("Agregar Nuevo Almacén");
            btnGuardar.setText("Guardar");
            
        }
    }

    // ------------------------------------------------------------------
    // 			MANEJADORES DE EVENTOS
    // ------------------------------------------------------------------
    
    /**
     * Cierra la ventana del formulario.
     */
    @FXML
    private void handleCancelar() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    /**
     * Valida los datos y guarda o actualiza el almacén.
     */
    @FXML
    private void handleGuardarAlmacen() {
        // 1. Validar campos obligatorios
        if (!validarCampos()) {
            return; // Detiene el proceso si hay errores de validación
        }
        
        // --- INICIO DE LA CORRECCIÓN ---
        // Se utiliza Integer.parseInt() para convertir el ID (String) a int,
        // ya que la variable 'idAlmacen' requiere un tipo primitivo int.
        int idAlmacen = -1;
        try {
            idAlmacen = esModificacion ? Integer.parseInt(almacenEnEdicion.getId()) : -1;
        } catch (NumberFormatException e) {
            mostrarAlertaError("Error de ID", "El ID del almacén no es un número válido. No se puede modificar.");
            return;
        }
        // --- FIN DE LA CORRECCIÓN ---
        
        // Obtener los valores del formulario
        String nombre = txtNombre.getText().trim();
        
        String usuarioActual = SessionContext.getCurrentUserName(); // Asumiendo que SessionContext existe

        try {
            // 2. LLAMADA A LA BASE DE DATOS
            if (esModificacion) {
                // Modo Modificar: Pasamos el ID del almacén existente
                DatabaseManager.actualizarAlmacen(
                    idAlmacen, 
                    nombre, 
                    usuarioActual
                );
                mostrarAlertaInfo("Éxito", "El almacén ID " + idAlmacen + " ha sido **modificado** exitosamente.");
            } else {
                // Modo Agregar: El ID es asignado por la DB
                DatabaseManager.insertarAlmacen(
                    nombre, 
                    usuarioActual
                );
                mostrarAlertaInfo("Éxito", "El nuevo almacén '" + nombre + "' ha sido **guardado** con éxito.");
            }
            
            // 3. Si todo fue bien, cerrar el formulario
            Stage stage = (Stage) btnGuardar.getScene().getWindow();
            stage.close();
            
        } catch (SQLException e) {
            mostrarAlertaError("Error de Base de Datos", "Ocurrió un error al guardar/actualizar el almacén: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
             mostrarAlertaError("Error Inesperado", "Ocurrió un error al procesar los datos: " + e.getMessage());
             e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------
    // 			MÉTODOS PRIVADOS DE UTILIDAD
    // ------------------------------------------------------------------
    
    /**
     * Valida que los campos requeridos no estén vacíos.
     */
    private boolean validarCampos() {
        String errorMsg = "";
        
        // Validación de Nombre
        if (txtNombre.getText().trim().isEmpty()) {
            errorMsg += "El Nombre del almacén es obligatorio.\n";
        }

        if (errorMsg.isEmpty()) {
            return true;
        } else {
            mostrarAlertaError("Errores de Validación", errorMsg);
            return false;
        }
    }
    
    // --- Métodos de Alerta Reutilizables ---
    
    private void mostrarAlertaError(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null); 
        alert.setContentText(contenido);
        alert.showAndWait();
    }
    
    private void mostrarAlertaInfo(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null); 
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}