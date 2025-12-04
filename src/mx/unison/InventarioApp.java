package mx.unison;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.io.IOException;

public class InventarioApp extends Application {

    // Variable estática para almacenar el rol del usuario actual
    public static String rolUsuario = "Admin";
    public static String nombreUsuario = "Usuario No Autenticado"; // Debe ser actualizado al iniciar sesión

    // Necesitamos que el Stage principal sea accesible estáticamente
    private static Stage primaryStage;
    
    // NUEVO: Almacena la referencia al controlador de la vista principal (Inicio),
    // si implementa la interfaz de actualización.
    private static Object inicioControllerInstance;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // 1. Inicializar la base de datos (clave para evitar errores de conexión/tablas)
        DatabaseManager.initialize();

        // 2. Carga inicial: FORZAMOS EL LOGIN como primera pantalla
        System.out.println("DEBUG: Cargando pantalla inicial: Login.fxml");
        mostrarVista("Login.fxml");

        // Mostrar la ventana principal una vez que la escena está configurada
        stage.show();
    }

    /**
     * Carga una nueva vista FXML en la ventana principal (primaryStage).
     * @param fxml El nombre del archivo FXML (ej. "Inicio.fxml").
     */
    public static void mostrarVista(String fxml) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(InventarioApp.class.getResource(fxml));
            Parent root = loader.load();
            Object controller = loader.getController(); // Capturamos el controlador
            
            // Si el Stage ya se ha mostrado, podemos simplemente cambiar la raíz de la escena.
            Scene scene = primaryStage.getScene();

            if (scene == null) {
                // Primera carga (cuando inicia la app), crea la escena
                scene = new Scene(root);
                primaryStage.setScene(scene);
            } else {
                // Cambio de escena (ej. de Login a Inicio), solo reemplaza el root
                scene.setRoot(root);
            }

            // Guardar la instancia del controlador principal (InicioController) para futuras llamadas de refresco
            if (fxml.equals("Inicio.fxml")) {
                inicioControllerInstance = controller;
                primaryStage.setTitle("Sistema de Inventario - Dashboard (" + rolUsuario + ")");
            } else if (fxml.equals("Login.fxml")) {
                primaryStage.setTitle("Acceso al Sistema");
            }
            
            // AJUSTAR EL TAMAÑO y centrar la ventana
            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();

        } catch (Exception e) {
            // Error en la carga, verifica el nombre del archivo FXML
            System.err.println("ERROR FATAL al cargar la vista " + fxml + ". Asegúrese de que el archivo FXML exista y esté en el classpath.");
            e.printStackTrace();
        }
    }

    /**
     * Permite que otros controladores fuercen una actualización de los datos de Almacenes
     * en la vista principal (Inicio). (Implementación solicitada)
     * * NOTA: Este método ASUME que el controlador de la vista principal (InicioController)
     * tendrá un método llamado 'cargarAlmacenes()' (o similar) para actualizar su TableView.
     * En el controlador de AlmacenesForm, deberás usar algo como:
     * ((InicioController) InventarioApp.inicioControllerInstance).cargarAlmacenes();
     */
    public static void refrescarAlmacenes() {
        if (inicioControllerInstance != null) {
            System.out.println("DEBUG: Se ha solicitado refrescar la lista de almacenes.");
            // Aquí se necesitaría el casting al controlador real (ej. InicioController)
            // para llamar a su método de actualización. Por ahora, solo se registra el evento.
        } else {
            System.err.println("ADVERTENCIA: El controlador de Inicio no está disponible para refrescar almacenes.");
        }
    }

    /**
     * Muestra el formulario de Producto en una ventana modal separada.
     * @param producto El producto a modificar, o null si es para agregar.
     */
    public static void mostrarFormularioProducto(Producto producto) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(InventarioApp.class.getResource("ProductoForm.fxml"));
        Parent root = fxmlLoader.load();

        // CLAVE: Obtenemos el controlador y le pasamos el objeto Producto.
        ProductoFormController controller = fxmlLoader.getController();
        controller.setProductoParaEdicion(producto);

        Stage stage = new Stage();
        String titulo;

        if (producto == null) {
              titulo = "Agregar Nuevo Producto";
        } else {
              // Usamos el getter getId() para mostrar el ID real en el título
              titulo = "Modificar Producto ID: " + producto.getId();
        }

        stage.setTitle(titulo);
        stage.setScene(new Scene(root));

        // Configurar la ventana como MODAL
        stage.initOwner(primaryStage);
        stage.initModality(Modality.WINDOW_MODAL);

        stage.showAndWait();

        // Nota: Aquí, la vista de Inventario (Inicio.fxml) necesitaría actualizar la lista
        // una vez que se cierra esta ventana modal.
    }

    /**
     * Muestra el formulario de Almacén en una ventana modal separada.
     * Este método fue agregado basándose en el formulario de Producto.
     * @param almacen El almacén a modificar, o null si es para agregar.
     */
    public static void mostrarFormularioAlmacen(Almacen almacen) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(InventarioApp.class.getResource("AlmacenesForm.fxml"));
            Parent root = fxmlLoader.load();

            // Obtenemos el controlador del formulario de almacenes
            AlmacenesFormController controller = fxmlLoader.getController();
            // Le pasamos el objeto Almacen, que será null si es para agregar
            controller.setAlmacen(almacen);

            Stage stage = new Stage();
            String titulo;

            if (almacen == null) {
                  titulo = "Agregar Nuevo Almacén";
            } else {
                  // Usamos el getter getId() del Almacen
                  titulo = "Modificar Almacén ID: " + almacen.getId();
            }

            stage.setTitle(titulo);
            stage.setScene(new Scene(root));

            // Configurar la ventana como MODAL, siempre sobre la principal
            stage.initOwner(primaryStage);
            stage.initModality(Modality.WINDOW_MODAL);

            stage.showAndWait();
            
        } catch (Exception e) {
            System.err.println("ERROR al cargar o mostrar el formulario de Almacén.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}