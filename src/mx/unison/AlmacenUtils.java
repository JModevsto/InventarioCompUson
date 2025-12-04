package mx.unison;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlmacenUtils {

    // Mapa que contiene la traducción de ID numérico (String) al nombre (String)
    private static final Map<String, String> MAPA_ALMACENES = new HashMap<>();
    
    // Bloque estático que se ejecuta una vez al inicio para llenar el mapa
    static {
        // Los IDs deben ser Strings para coincidir con el tipo que viene de la base de datos
        MAPA_ALMACENES.put("1", "Hermosillo");
        MAPA_ALMACENES.put("2", "Caborca");
        MAPA_ALMACENES.put("3", "Guaymas");
        MAPA_ALMACENES.put("4", "Sonoita");
        MAPA_ALMACENES.put("5", "Nogales");
    }

    /**
     * Convierte el ID numérico del almacén al nombre legible.
     */
    public static String getNombreAlmacen(String idAlmacen) {
        if (idAlmacen == null || idAlmacen.trim().isEmpty()) {
            return "Desconocido";
        }
        return MAPA_ALMACENES.getOrDefault(idAlmacen.trim(), "Desconocido");
    }
    
    /**
     * Devuelve una lista de los nombres de los almacenes para llenar un ComboBox.
     */
    public static List<String> getNombresAlmacenes() {
        // Retorna solo los valores (los nombres legibles) del mapa
        return new ArrayList<>(MAPA_ALMACENES.values());
    }
    
    /**
     * Convierte el nombre legible al ID numérico del almacén.
     */
    public static String getIdAlmacen(String nombreAlmacen) {
    // Buscar el ID basado en el nombre (valor)
    for (Map.Entry<String, String> entry : MAPA_ALMACENES.entrySet()) {
        if (entry.getValue().equalsIgnoreCase(nombreAlmacen)) {
            return entry.getKey();
        }
    }
    return null; // Si no se encuentra el nombre
}
}