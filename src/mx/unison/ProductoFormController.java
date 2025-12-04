package mx.unison;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class ProductoFormController {

    // --- Inyecciones FXML ---
    @FXML private Label lblTitulo;
    // ELIMINADO: @FXML private TextField txtID;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtCantidad;
    @FXML private ComboBox<String> cmbDepartamento;
    @FXML private ComboBox<String> cmbAlmacen;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    // --- Variables de Estado ---
    private Producto productoEnEdicion; // Será null para "Agregar"
    private boolean esModificacion = false;

    // ------------------------------------------------------------------
    //             MÉTODOS DE INICIALIZACIÓN
    // ------------------------------------------------------------------
    
    @FXML
    public void initialize() {
        // Inicializar ComboBoxes con datos fijos y de la utilidad
        cargarComboBoxes();
        
        // ELIMINADO: txtID.setEditable(false);
    }
    
    /**
     * Carga los datos fijos (Departamentos) y de la utilidad (Almacenes)
     */
    private void cargarComboBoxes() {
        // Departamentos
        List<String> departamentos = Arrays.asList("Materiales", "Mobiliario", "Impresion3D", "Computación");
        cmbDepartamento.setItems(FXCollections.observableArrayList(departamentos));
        
        
        try {
            // Intenta obtener los nombres de los almacenes desde la base de datos
            // Nota: Aquí se asume que DatabaseManager.obtenerNombresAlmacenes() llama a AlmacenUtils.getNombresAlmacenes()
            List<String> nombresAlmacenes = DatabaseManager.obtenerNombresAlmacenes();
            cmbAlmacen.setItems(FXCollections.observableArrayList(nombresAlmacenes));
            System.out.println("ComboBoxes de almacenes cargados con éxito.");

        } catch (SQLException e) {
            // Captura la excepción y la maneja
            
            // 1. Mostrar un mensaje de error en la interfaz gráfica
            mostrarAlertaError("Error de Carga de Datos", "No se pudo cargar la lista de almacenes.\nRevise la conexión a la base de datos.");
            
            // 2. Cargar una lista de error en el ComboBox y loguear el error para el desarrollador
            System.err.println("ERROR de DB: No se pudieron cargar los almacenes. Detalles: " + e.getMessage());
            cmbAlmacen.setItems(FXCollections.observableArrayList("ERROR al cargar datos"));
        }
    }

    /**
     * Método llamado por InventarioApp para preparar el formulario para edición o agregar.
     * @param producto El Producto a cargar, o null si es modo "Agregar".
     */
    public void setProductoParaEdicion(Producto producto) {
        this.productoEnEdicion = producto;
        
        if (productoEnEdicion != null) {
            // Modo MODIFICAR
            esModificacion = true;
            lblTitulo.setText("Modificar Producto ID: " + producto.getId());
            btnGuardar.setText("Actualizar");
            
            // Llenar los campos con los datos del producto
            // txtID.setText(producto.getId()); // Eliminamos la referencia a txtID
            txtNombre.setText(producto.getNombre());
            txtPrecio.setText(producto.getPrecio());
            txtCantidad.setText(producto.getCantidad());
            
            // Seleccionar valores existentes en ComboBoxes
            cmbDepartamento.getSelectionModel().select(producto.getDepartamento());
            
            // CLAVE: Convertir el ID del almacén (ej. "1") al Nombre (ej. "Hermosillo") para el ComboBox
            String nombreAlmacen = AlmacenUtils.getNombreAlmacen(producto.getAlmacen());
            cmbAlmacen.getSelectionModel().select(nombreAlmacen);
            
            // En modo modificación, el ID no se puede cambiar (ya no es necesario, pero lo mantenemos para referencia)
            // txtID.setDisable(true); 
            
        } else {
            // Modo AGREGAR
            esModificacion = false;
            lblTitulo.setText("Agregar Nuevo Producto");
            btnGuardar.setText("Guardar");
            
            // ELIMINADO: txtID.setDisable(false);
            // ELIMINADO: txtID.setEditable(true);
        }
    }

    // ------------------------------------------------------------------
    //             MANEJADORES DE EVENTOS
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
     * Valida los datos y guarda o actualiza el producto.
     */
    @FXML
    private void handleGuardarProducto() {
        // 1. Validar campos obligatorios (sin incluir ID para Agregar)
        if (!validarCampos()) {
            return; // Detiene el proceso si hay errores de validación
        }
        
        // 2. Obtener el ID existente (solo en modificación) o null (en agregar)
        String idExistente = esModificacion ? productoEnEdicion.getId() : null;

        try {
            // 3. VALIDACIÓN DE UNICIDAD DEL ID ELIMINADA. La DB se encargará del auto-incremento.
            
            // 4. Construir el objeto Producto
            Producto productoAGuardar = construirProducto(idExistente);

            // 5. LLAMADA A LA BASE DE DATOS
            if (esModificacion) {
                DatabaseManager.actualizarProducto(productoAGuardar);
                mostrarAlertaInfo("Éxito", "El producto ID " + idExistente + " ha sido **modificado** exitosamente.");
            } else {
                // En modo AGREGAR, la DB genera el ID
                DatabaseManager.agregarProducto(productoAGuardar);
                mostrarAlertaInfo("Éxito", "El nuevo producto ha sido **guardado** con éxito.");
            }
            
            // 6. Si todo fue bien, cerrar el formulario
            Stage stage = (Stage) btnGuardar.getScene().getWindow();
            stage.close();
            
        } catch (SQLException e) {
            mostrarAlertaError("Error de Base de Datos", "Ocurrió un error al guardar/actualizar el producto: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
             mostrarAlertaError("Error Inesperado", "Ocurrió un error al procesar los datos: " + e.getMessage());
             e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------
    //             MÉTODOS PRIVADOS DE UTILIDAD
    // ------------------------------------------------------------------
    
    /**
     * Construye un objeto Producto a partir de los datos del formulario.
     */
    private Producto construirProducto(String id) {
        // Obtener el ID del almacén (Foreign Key)
        String nombreAlmacen = cmbAlmacen.getSelectionModel().getSelectedItem();
        
        // CLAVE: Convertir el Nombre (ej. "Hermosillo") al ID (ej. "1") para guardarlo en la DB
        String idAlmacen = AlmacenUtils.getIdAlmacen(nombreAlmacen);
        
        // Creamos un nuevo objeto Producto con los datos actuales del formulario
        return new Producto(
            id, // Será null si es Agregar, o el ID existente si es Modificar
            txtNombre.getText().trim(),
            txtPrecio.getText().trim(),
            txtCantidad.getText().trim(),
            cmbDepartamento.getSelectionModel().getSelectedItem(),
            idAlmacen, // Guardamos el ID del almacén (FK)
            // Los siguientes campos se inicializan vacíos ya que se llenarán o actualizarán en la DB
            "", // fechaCreacion
            "", // fechaModificacion
            ""	// ultimoUsuario
        );
    }
    
    /**
     * Valida que los campos requeridos no estén vacíos y que los campos numéricos sean válidos.
     * (El campo ID es excluido de esta validación para el modo Agregar)
     */
    private boolean validarCampos() {
        String errorMsg = "";
        
        // ELIMINADA la validación de ID para modo Agregar
        
        // Validación de Nombre
        if (txtNombre.getText().trim().isEmpty()) {
            errorMsg += "El Nombre del producto es obligatorio.\n";
        }
        
        // Validación de ComboBoxes
        if (cmbDepartamento.getSelectionModel().isEmpty()) {
            errorMsg += "Debe seleccionar un Departamento.\n";
        }
        if (cmbAlmacen.getSelectionModel().isEmpty()) {
            errorMsg += "Debe seleccionar un Almacén.\n";
        }
        
        // Validación Numérica de Precio
        try {
            double precio = Double.parseDouble(txtPrecio.getText().trim());
            if (precio <= 0) {
                 errorMsg += "El Precio debe ser un número positivo (mayor a 0).\n";
            }
        } catch (NumberFormatException e) {
            errorMsg += "El Precio debe ser un valor numérico válido.\n";
        }
        
        // Validación Numérica de Cantidad
        try {
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            if (cantidad < 0) {
                 errorMsg += "La Cantidad no puede ser negativa.\n";
            }
        } catch (NumberFormatException e) {
            errorMsg += "La Cantidad debe ser un número entero válido.\n";
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