package mx.unison;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList; 
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import java.util.Optional;
import mx.unison.SessionContext;

public class ProductosController {

    // --- 1. INYECCIÓN DE LA TABLA Y LAS 9 COLUMNAS ---
    @FXML private TableView<Producto> tblProductos;

    @FXML private TableColumn<Producto, String> colID;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colPrecio;
    @FXML private TableColumn<Producto, String> colCantidad;
    @FXML private TableColumn<Producto, String> colDepartamento;
    @FXML private TableColumn<Producto, String> colAlmacen; 
    @FXML private TableColumn<Producto, String> colFechaCreacion;
    @FXML private TableColumn<Producto, String> colFechaModificacion;
    @FXML private TableColumn<Producto, String> colUltimoUsuario;
    
    // --- 2. INYECCIÓN DE FILTROS ---
    @FXML private TextField txtFiltroNombre;
    @FXML private ComboBox<String> cmbDepartamentoFiltro;
    
    @FXML private ComboBox<String> cmbAlmacenFiltro; 
    @FXML private TextField txtID; 
    
    @FXML private TextField txtPrecioMin;
    @FXML private TextField txtPrecioMax;
    @FXML private TextField txtCantidadMin;
    @FXML private TextField txtCantidadMax;
    @FXML private Button btnAplicarFiltros;

    // --- 3. INYECCIÓN DE BOTONES DE ACCIÓN Y BARRA INFERIOR ---
    @FXML private HBox bottomBar; 
    @FXML private Button btnAgregar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;
    @FXML private Button btnVolver; 

    // ------------------------------------------------------------------
    //              MÉTODOS DE INICIALIZACIÓN Y CONFIGURACIÓN
    // ------------------------------------------------------------------

    @FXML
    public void initialize() {
        // 1. Configurar ComboBoxes
        List<String> departamentos = Arrays.asList("TODOS", "Materiales", "Mobiliario", "Impresion3D", "Computación");
        cmbDepartamentoFiltro.setItems(javafx.collections.FXCollections.observableArrayList(departamentos));
        cmbDepartamentoFiltro.getSelectionModel().selectFirst();
        
        try {
            // ERROR SOLUCIONADO: Ahora DatabaseManager tiene obtenerNombresAlmacenes()
            List<String> nombresAlmacenes = DatabaseManager.obtenerNombresAlmacenes(); 
            nombresAlmacenes.add(0, "TODOS");
            cmbAlmacenFiltro.setItems(javafx.collections.FXCollections.observableArrayList(nombresAlmacenes));
            cmbAlmacenFiltro.getSelectionModel().selectFirst();
        // CATCH CORREGIDO: Maneja la SQLException que lanza el método de la DB
        } catch (SQLException e) { 
            mostrarAlertaError("Error de DB", "No se pudieron cargar los nombres de almacenes para el filtro.");
            e.printStackTrace();
        }
        
        // ----------------------------------------------------
        // LÓGICA DE SEGURIDAD BASADA EN ROL 
        // ----------------------------------------------------
        
        // Usamos la referencia completa a la clase InventarioApp para mayor robustez
        String rolActual = SessionContext.getCurrentUserRole();
        
        boolean tienePermisoCRUD = "Admin".equalsIgnoreCase(rolActual) || "Productos".equalsIgnoreCase(rolActual);
        
        bottomBar.setManaged(tienePermisoCRUD);
        bottomBar.setVisible(tienePermisoCRUD);
        
        // ----------------------------------------------------
        
        // 2. Conexión de las Columnas a las Propiedades del Modelo Producto Y COMPARADORES
        configurarColumnas(); 
        
        // 3. Cargar datos iniciales sin filtros
        cargarDatosProductos(null); 
        
        // 4. Conexión del botón de filtros
        btnAplicarFiltros.setOnAction(e -> handleAplicarFiltros());
    }
    
    /**
     * Define la propiedad de la clase Producto que cada columna debe mostrar
     * y establece comparadores numéricos para las columnas de precio y cantidad.
     */
    private void configurarColumnas() {
        // Conexión de propiedades (Requiere que Producto.java esté bien definido)
        colID.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        colNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colPrecio.setCellValueFactory(cellData -> cellData.getValue().precioProperty());
        colCantidad.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty());
        colDepartamento.setCellValueFactory(cellData -> cellData.getValue().departamentoProperty());
        colAlmacen.setCellValueFactory(cellData -> cellData.getValue().almacenProperty()); 
        colFechaCreacion.setCellValueFactory(cellData -> cellData.getValue().fechaCreacionProperty());
        colFechaModificacion.setCellValueFactory(cellData -> cellData.getValue().fechaModificacionProperty());
        colUltimoUsuario.setCellValueFactory(cellData -> cellData.getValue().ultimoUsuarioProperty());
        
        // Comparadores (la lógica es correcta)
        colPrecio.setComparator((s1, s2) -> {
            try {
                return Double.compare(Double.parseDouble(s1), Double.parseDouble(s2));
            } catch (NumberFormatException e) {
                return s1.compareTo(s2); 
            }
        });

        colCantidad.setComparator((s1, s2) -> {
            try {
                return Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2));
            } catch (NumberFormatException e) {
                return s1.compareTo(s2);
            }
        });
        
        colID.setComparator((s1, s2) -> {
            try {
                return Integer.compare(Integer.parseInt(s1), Integer.parseInt(s2));
            } catch (NumberFormatException e) {
                return s1.compareTo(s2);
            }
        });
    }

    /**
     * Implementación del botón Volver: regresa a la pantalla de Inicio.
     */
    @FXML
    private void handleVolverInicio(ActionEvent event) {
        try {
            mx.unison.InventarioApp.mostrarVista("Inicio.fxml"); 
        } catch (IOException e) {
            System.err.println("Error al regresar a la vista de Inicio: " + e.getMessage());
        }
    }
    
    // ------------------------------------------------------------------
    //              MÉTODOS DE DATOS Y FILTROS
    // ------------------------------------------------------------------

    /**
     * Recoge los valores de los filtros y llama al DatabaseManager.
     */
    @FXML
    private void handleAplicarFiltros() {
        String id = txtID.getText().trim(); 
        String nombre = txtFiltroNombre.getText().trim();
        String departamento = cmbDepartamentoFiltro.getSelectionModel().getSelectedItem();
        String almacenNombre = cmbAlmacenFiltro.getSelectionModel().getSelectedItem(); 
        
        String precioMin = txtPrecioMin.getText().trim();
        String precioMax = txtPrecioMax.getText().trim();
        String cantidadMin = txtCantidadMin.getText().trim();
        String cantidadMax = txtCantidadMax.getText().trim();
        
        String idAlmacen = almacenNombre.equals("TODOS") ? null : AlmacenUtils.getIdAlmacen(almacenNombre);

        List<String> filtros = Arrays.asList(
            nombre,
            departamento.equals("TODOS") ? null : departamento,
            precioMin.isEmpty() ? null : precioMin,
            precioMax.isEmpty() ? null : precioMax,
            cantidadMin.isEmpty() ? null : cantidadMin,
            cantidadMax.isEmpty() ? null : cantidadMax,
            idAlmacen, 
            id.isEmpty() ? null : id 
        );

        cargarDatosProductos(filtros);
    }
    
    /**
     * Carga los datos de la DB a la TableView usando el DatabaseManager.
     */
    public void cargarDatosProductos(List<String> filtros) {
        try {
            ObservableList<Producto> productos = DatabaseManager.obtenerProductosFiltrados(filtros);
            tblProductos.setItems(productos);
        } catch (SQLException e) {
            // El error estaba en la línea de 'e.printStackTrace()' si no se podía resolver 'e'
            System.err.println("Error al cargar productos desde la base de datos.");
            e.printStackTrace();
            mostrarAlertaError("Error de Conexión", "No se pudieron cargar los datos del inventario. " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    //              MÉTODOS DE ACCIÓN (CRUD)
    // ------------------------------------------------------------------
    
// Método para abrir el formulario de AGREGAR
@FXML
private void handleAgregarProducto() {
    try {
        // Llamada a InventarioApp.mostrarFormularioProducto (problema de referencia corregido)
        mx.unison.InventarioApp.mostrarFormularioProducto(null); 
        // Recargar datos para mostrar el nuevo producto inmediatamente
        cargarDatosProductos(null); 
    } catch (IOException e) {
        System.err.println("Error al cargar la vista de formulario de Agregar.");
        e.printStackTrace();
        mostrarAlertaError("Error de Vista", "No se pudo abrir el formulario de producto.");
    }
}

// Método para abrir el formulario de MODIFICAR
@FXML
private void handleModificarProducto() {
    // 1. Obtener el producto seleccionado
    Producto productoSeleccionado = tblProductos.getSelectionModel().getSelectedItem();
    
    if (productoSeleccionado != null) {
        try {
            // Llamada a InventarioApp.mostrarFormularioProducto (problema de referencia corregido)
            mx.unison.InventarioApp.mostrarFormularioProducto(productoSeleccionado);
            // Recargar datos para mostrar los cambios inmediatamente
            cargarDatosProductos(null); 
        } catch (IOException e) {
            System.err.println("Error al cargar la vista de formulario de Modificar.");
            e.printStackTrace();
            mostrarAlertaError("Error de Vista", "No se pudo abrir el formulario de producto.");
        }
    } else {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Sin Selección");
        alert.setHeaderText(null);
        alert.setContentText("Por favor, selecciona un producto de la tabla para modificarlo.");
        alert.showAndWait();
    }
}
    
    // Método para ELIMINAR
    @FXML
    private void handleEliminarProducto() {
        Producto productoSeleccionado = tblProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado != null) {
            try {
                // 1. Pedir confirmación
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirmar Eliminación");
                // Los métodos getNombre() y getId() deberían funcionar ahora si Producto.java es correcto.
                confirm.setHeaderText("Eliminar Producto: " + productoSeleccionado.getNombre()); 
                confirm.setContentText("¿Está seguro de que desea eliminar el producto con ID: " + productoSeleccionado.getId() + "? Esta acción no se puede deshacer.");

                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        try {
                            DatabaseManager.eliminarProducto(productoSeleccionado.getId());
                            mostrarAlertaInfo("Éxito", "El producto fue eliminado correctamente.");
                            // 3. Recargar datos
                            cargarDatosProductos(null); 
                        } catch (SQLException e) {
                            mostrarAlertaError("Error de DB", "No se pudo eliminar el producto: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                 mostrarAlertaError("Error", "Ocurrió un error inesperado al intentar eliminar: " + e.getMessage());
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Sin Selección");
            alert.setHeaderText(null);
            alert.setContentText("Por favor, selecciona un producto de la tabla para eliminarlo.");
            alert.showAndWait();
        }
    }
    /** Muestra una alerta de error (utilidad) */
    private void mostrarAlertaError(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null); 
        alert.setContentText(contenido);
        alert.showAndWait();
    }
    
    /** Muestra una alerta de información (utilidad) */
    private void mostrarAlertaInfo(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null); 
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}