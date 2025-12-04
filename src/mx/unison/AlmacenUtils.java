package mx.unison;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Clase de utilidad para manejar la lista de Almacenes en caché.
 * Los datos se cargan desde la base de datos para asegurar que las nuevas ubicaciones
 * sean visibles inmediatamente en los formularios y filtros.
 */
public class AlmacenUtils {

    // Mapa que contiene la traducción de ID numérico (String) al nombre (String)
    // Ya no es FINAL, ya que debe actualizarse dinámicamente.
    private static Map<String, String> almacenIdToNombreCache = new HashMap<>();
    
    // Mapa inverso para encontrar el ID a partir del nombre (útil para el filtro)
    private static Map<String, String> almacenNombreToIdCache = new HashMap<>();
    
    // Lista simple de nombres para llenar ComboBoxes (útil para el método getNombresAlmacenes)
    private static List<String> nombresAlmacenesCache = new ArrayList<>();

    // Bloque estático: Inicia la carga de datos al arrancar la aplicación.
    static {
        try {
            recargarCacheAlmacenes();
        } catch (SQLException e) {
            System.err.println("Error FATAL al cargar la caché inicial de almacenes: " + e.getMessage());
            // En una aplicación real, esto requeriría cerrar o deshabilitar la funcionalidad de inventario.
        }
    }

    // Constructor privado para evitar instanciación
    private AlmacenUtils() {} 

    /**
     * MÉTODO CRUCIAL: Fuerza la recarga de todos los almacenes desde la base de datos.
     * DEBE llamarse después de AGREGAR, MODIFICAR o ELIMINAR un almacén.
     */
    public static void recargarCacheAlmacenes() throws SQLException {
        // Asumimos que tienes un método en DatabaseManager que devuelve List<Almacen>
        // Este método debe obtener todos los campos, incluido el ID.
        List<Almacen> almacenes = DatabaseManager.obtenerTodosLosAlmacenes(); 
        
        // Limpiamos los mapas antes de recargar
        almacenIdToNombreCache.clear();
        almacenNombreToIdCache.clear();
        
        // 1. Reconstruir el caché de ID -> Nombre y Nombre -> ID
        for (Almacen almacen : almacenes) {
            String id = almacen.getId();
            String nombre = almacen.getNombre();
            
            almacenIdToNombreCache.put(id, nombre);
            almacenNombreToIdCache.put(nombre, id);
        }
            
        // 2. Reconstruir la lista simple de nombres (para ComboBoxes)
        nombresAlmacenesCache = new ArrayList<>(almacenIdToNombreCache.values());
            
        System.out.println("DEBUG: Caché de Almacenes recargada correctamente. Total: " + almacenIdToNombreCache.size() + " elementos.");
    }
    
    /**
     * Convierte el ID numérico del almacén al nombre legible.
     * Utiliza la caché dinámica.
     */
    public static String getNombreAlmacen(String idAlmacen) {
        if (idAlmacen == null || idAlmacen.trim().isEmpty()) {
            return "Desconocido";
        }
        return almacenIdToNombreCache.getOrDefault(idAlmacen.trim(), "Desconocido");
    }
    
    /**
     * Devuelve una lista de los nombres de los almacenes para llenar un ComboBox.
     * Utiliza la caché dinámica.
     */
    public static List<String> getNombresAlmacenes() {
        return nombresAlmacenesCache;
    }
    
    /**
     * Convierte el nombre legible al ID numérico del almacén.
     * Utiliza la caché dinámica.
     */
    public static String getIdAlmacen(String nombreAlmacen) {
        return almacenNombreToIdCache.getOrDefault(nombreAlmacen, null);
    }
}