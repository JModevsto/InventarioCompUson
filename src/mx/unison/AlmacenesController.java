package mx.unison;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javafx.event.ActionEvent;
import java.util.Optional;
import mx.unison.SessionContext;

/**
 * Controlador para la vista de Almacenes. Gestiona la tabla, filtros
 * y la seguridad de los botones CRUD según el rol del usuario.
 */
public class AlmacenesController {

    // --- 1. INYECCIÓN DE LA TABLA Y LAS 5 COLUMNAS ---
    @FXML private TableView<Almacen> tblAlmacenes;
    // CORRECCIÓN: El ID se maneja como StringProperty en el modelo, por lo que debe ser String.
    @FXML private TableColumn<Almacen, String> colID;	
    @FXML private TableColumn<Almacen, String> colNombre;
    @FXML private TableColumn<Almacen, String> colFechaCreacion;
    @FXML private TableColumn<Almacen, String> colFechaModificacion;
    @FXML private TableColumn<Almacen, String> colUltimoUsuario;
    
    // --- 2. INYECCIÓN DE FILTROS ---
    @FXML private ComboBox<String> cmbAlmacenFiltro; // Filtro por nombre de Almacén
    @FXML private TextField txtID; // Filtro por ID
    @FXML private Button btnAplicarFiltros;

    // --- 3. INYECCIÓN DE BOTONES DE ACCIÓN Y BARRA INFERIOR ---
    @FXML private HBox bottomBar;
    @FXML private Button btnAgregar;
    @FXML private Button btnModificar;
    @FXML private Button btnEliminar;	
    @FXML private Button btnVolver;

    // ------------------------------------------------------------------
    //                          MÉTODOS DE INICIALIZACIÓN Y CONFIGURACIÓN
    // ------------------------------------------------------------------

    @FXML
    public void initialize() {
        // 1. Configurar ComboBox de Almacenes con todos los nombres disponibles
        try {
            // Se asume que AlmacenUtils.recargarCacheAlmacenes() ya se llamó en el bloque estático de AlmacenUtils.
            List<String> nombresAlmacenes = AlmacenUtils.getNombresAlmacenes(); 
            nombresAlmacenes.add(0, "TODOS");
            cmbAlmacenFiltro.setItems(javafx.collections.FXCollections.observableArrayList(nombresAlmacenes));
            cmbAlmacenFiltro.getSelectionModel().selectFirst();
        // CATCH CORREGIDO: Maneja la SQLException que lanza el método de la DB
        } catch (Exception e) { // Usamos Exception por si hay otros errores de inicialización
            mostrarAlertaError("Error de Carga", "No se pudieron cargar los nombres de almacenes para el filtro.");
            e.printStackTrace();
        }

        // ----------------------------------------------------
        // LÓGICA DE SEGURIDAD BASADA EN ROL (CLAVE)
        // ----------------------------------------------------
        
        String rolActual = SessionContext.getCurrentUserRole();
        
        // El permiso CRUD ahora incluye "Admin" y "Almacenes"
        boolean tienePermisoCRUD = "Admin".equalsIgnoreCase(rolActual) || "Almacenes".equalsIgnoreCase(rolActual);
        
        System.out.println("DEBUG - Rol Actual en AlmacenesController: " + rolActual + ". Permiso CRUD: " + tienePermisoCRUD);
        
        // Oculta/Muestra la barra de botones (bottomBar)
        bottomBar.setManaged(tienePermisoCRUD);
        bottomBar.setVisible(tienePermisoCRUD);
        
        // ----------------------------------------------------
        
        // 2. Conexión de las Columnas a las Propiedades del Modelo Almacen
        configurarColumnas();
        
        // 3. Cargar datos iniciales sin filtros
        cargarDatosAlmacenes(null);
        
        // 4. Conexión del botón de filtros
        btnAplicarFiltros.setOnAction(e -> handleAplicarFiltros());
    }
    
    /**
     * Define la propiedad de la clase Almacen que cada columna debe mostrar.
     */
    private void configurarColumnas() {
        // Conexión de propiedades (Requiere que Almacen.java esté bien definido)
        colID.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        colNombre.setCellValueFactory(cellData -> cellData.getValue().nombreProperty());
        colFechaCreacion.setCellValueFactory(cellData -> cellData.getValue().fechaCreacionProperty());
        colFechaModificacion.setCellValueFactory(cellData -> cellData.getValue().fechaModificacionProperty());
        colUltimoUsuario.setCellValueFactory(cellData -> cellData.getValue().ultimoUsuarioProperty());
        
        // CORRECCIÓN: El comparador recibe Strings (s1, s2) porque la columna es String.
        // Se mantiene la lógica de intentar parsear a Integer para un ordenamiento numérico.
        colID.setComparator((s1, s2) -> {
            try {
                // SOLUCIONADO: Ahora compara Strings. El error de compareTo desaparece.
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
            // Asegúrate de usar la referencia completa a la clase InventarioApp
            mx.unison.InventarioApp.mostrarVista("Inicio.fxml");	
        } catch (IOException e) {
            System.err.println("Error al regresar a la vista de Inicio: " + e.getMessage());
        }
    }
    
    // ------------------------------------------------------------------
    //                          MÉTODOS DE DATOS Y FILTROS
    // ------------------------------------------------------------------

    /**
     * Recoge los valores de los filtros (ID y Nombre de Almacén) y llama al DatabaseManager.
     */
    @FXML
    private void handleAplicarFiltros() {
        String id = txtID.getText().trim().isEmpty() ? null : txtID.getText().trim();	
        String nombre = cmbAlmacenFiltro.getSelectionModel().getSelectedItem();
        
        // Si se selecciona "TODOS", el nombre es nulo para la consulta de DB.
        String filtroNombre = "TODOS".equalsIgnoreCase(nombre) ? null : nombre;	

        // Los filtros son [nombre, id]
        List<String> filtros = Arrays.asList(filtroNombre, id);

        cargarDatosAlmacenes(filtros);
    }
    
    /**
     * Carga los datos de la DB a la TableView usando el DatabaseManager.
     */
    public void cargarDatosAlmacenes(List<String> filtros) {
        try {
            // ERROR SOLUCIONADO: Ahora DatabaseManager tiene obtenerAlmacenesFiltrados()
            ObservableList<Almacen> almacenes = DatabaseManager.obtenerAlmacenesFiltrados(filtros);
            tblAlmacenes.setItems(almacenes);
        // CATCH CORREGIDO: Maneja la SQLException que lanza el método de la DB
        } catch (SQLException e) {	
            System.err.println("Error al cargar almacenes desde la base de datos.");
            e.printStackTrace();
            mostrarAlertaError("Error de Conexión", "No se pudieron cargar los datos de almacenes. " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    //                          MÉTODOS DE ACCIÓN (CRUD)
    // ------------------------------------------------------------------
    
    /** Abre el formulario para agregar un nuevo almacén. */
    @FXML
    private void handleAgregarAlmacen() {
        try {
            // Abre el formulario modal (InventarioApp.mostrarFormularioAlmacen debe usar showAndWait())
            mx.unison.InventarioApp.mostrarFormularioAlmacen(null);	
            
            // LLAMADA CRUCIAL: Recargar caché después de que el formulario se cierra (y presumiblemente guarda)
            AlmacenUtils.recargarCacheAlmacenes(); 
            
            // Refresca la tabla local
            cargarDatosAlmacenes(null);	
        // CATCH CORREGIDO: Maneja la IOException
        } catch (IOException | SQLException e) { // Añadimos SQLException por si falla la recarga
            System.err.println("Error al cargar la vista de formulario de Agregar Almacén o al refrescar la caché.");
            e.printStackTrace();
            mostrarAlertaError("Error de Operación", "No se pudo completar la operación de agregar almacén: " + e.getMessage());
        }
    }

    /** Abre el formulario para modificar el almacén seleccionado. */
    @FXML
    private void handleModificarAlmacen() {
        Almacen almacenSeleccionado = tblAlmacenes.getSelectionModel().getSelectedItem();
        
        if (almacenSeleccionado != null) {
            try {
                // Abre el formulario modal (InventarioApp.mostrarFormularioAlmacen debe usar showAndWait())
                mx.unison.InventarioApp.mostrarFormularioAlmacen(almacenSeleccionado);
                
                // LLAMADA CRUCIAL: Recargar caché después de que el formulario se cierra (y presumiblemente guarda)
                AlmacenUtils.recargarCacheAlmacenes(); 
                
                // Refresca la tabla local
                cargarDatosAlmacenes(null);
            // CATCH CORREGIDO: Maneja la IOException y SQLException
            } catch (IOException | SQLException e) {	
                System.err.println("Error al cargar la vista de formulario de Modificar Almacén o al refrescar la caché.");
                e.printStackTrace();
                mostrarAlertaError("Error de Operación", "No se pudo completar la operación de modificar almacén: " + e.getMessage());
            }
        } else {
            mostrarAlertaAdvertencia("Sin Selección", "Por favor, selecciona un almacén de la tabla para modificarlo.");
        }
    }
    
    /** Elimina el almacén seleccionado de la tabla. */
    @FXML
    private void handleEliminarAlmacen() {
        Almacen almacenSeleccionado = tblAlmacenes.getSelectionModel().getSelectedItem();
        if (almacenSeleccionado != null) {
            
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar Eliminación Masiva");
            confirm.setHeaderText("Eliminar Almacén: " + almacenSeleccionado.getNombre());
            
            // TEXTO DE ADVERTENCIA ACTUALIZADO
            confirm.setContentText(
                "ADVERTENCIA: Todos los productos relacionados con este almacén serán ELIMINADOS permanentemente." +
                "\n\n¿Desea proceder con la eliminación del almacén con ID: " + almacenSeleccionado.getId() + "?"
            );

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // DB Operation
                        DatabaseManager.eliminarAlmacen(almacenSeleccionado.getId());
                        
                        // LLAMADA CRUCIAL: Recargar caché inmediatamente después de la eliminación
                        AlmacenUtils.recargarCacheAlmacenes(); 
                        
                        mostrarAlertaInfo("Éxito", "El almacén fue eliminado y la caché refrescada correctamente.");
                        cargarDatosAlmacenes(null);	
                    // CATCH CORREGIDO: Maneja la SQLException
                    } catch (SQLException e) {	
                        mostrarAlertaError("Error de DB", "No se pudo eliminar el almacén: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        } else {
            mostrarAlertaAdvertencia("Sin Selección", "Por favor, selecciona un almacén de la tabla para eliminarlo.");
        }
    }
    
    // ------------------------------------------------------------------
    //                          MÉTODOS DE UTILIDAD
    // ------------------------------------------------------------------
    
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
    
    private void mostrarAlertaAdvertencia(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}