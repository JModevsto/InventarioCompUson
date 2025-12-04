package mx.unison;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DatabaseManager {
    // CAMBIO IMPORTANTE: Se eliminaron los caracteres invisibles de "espacio sin ruptura" (\u00a0)
    // de todo el archivo. Esto corrigió el "error de símbolo" de compilación.

    private static final String URL_CONEXION = "jdbc:sqlite:InventarioBD.db";

    // Mapeo de IDs de Almacén a Nombres para la población inicial
    private static final Map<String, String> ALMACENES_INICIALES = Map.of(
        "1", "Hermosillo",
        "2", "Caborca",
        "3", "Guaymas",
        "4", "Sonoita",
        "5", "Nogales"
    );

    // ------------------------------------------------------------------
    //                       MÉTODOS DE UTILIDAD
    // ------------------------------------------------------------------

    /**
     * Genera la fecha y hora actual formateada en la zona horaria de Sonora/Arizona (MST, GMT-7).
     * (Fix Issue 3)
     */
    private static String getLocalizedNow() {
        // Zona Horaria de Hermosillo, Sonora, que es la misma que Phoenix, Arizona (MST, GMT-7) 
        // y no usa DST.
        ZoneId sonoranZone = ZoneId.of("America/Phoenix");
        LocalDateTime now = LocalDateTime.now(sonoranZone);
        
        // Formato estándar para SQLite
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }
    
    /**
     * Calcula el siguiente ID consecutivo disponible para la tabla 'almacenes'.
     * Necesario porque la tabla usa TEXT PRIMARY KEY y no auto-increment.
     */
    private static int getNextAlmacenId() throws SQLException {
        // MAX(CAST(id AS INTEGER)) busca el ID numérico más alto y lo incrementa en 1.
        String sql = "SELECT MAX(CAST(id AS INTEGER)) FROM almacenes";
        int maxId = 0;
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                maxId = rs.getInt(1); 
            }
        }
        return maxId + 1;
    }
    
    // ------------------------------------------------------------------
    //                       MÉTODOS DE INICIALIZACIÓN
    // ------------------------------------------------------------------

    /**
     * Inicializa la conexión de la DB y asegura que todas las tablas existan y estén pobladas.
     */
    public static void initialize() {
        try {
            Class.forName("org.sqlite.JDBC"); 
            createTables();
            populateAlmacenes(); 
        } catch (ClassNotFoundException e) {
            System.err.println("Error: Driver de SQLite no encontrado.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error al inicializar o crear tablas en la base de datos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Crea las tablas 'almacenes' y 'productos' si no existen.
     * Importante: Se actualiza 'almacenes' para incluir campos de auditoría.
     */
    private static void createTables() throws SQLException {
        // 1. Crear tabla de almacenes con campos de auditoría (NUEVO)
        String sqlAlmacenes = "CREATE TABLE IF NOT EXISTS almacenes ("
                + "id TEXT PRIMARY KEY,"
                + "nombre TEXT NOT NULL UNIQUE,"
                + "fecha_hora_creacion TEXT NOT NULL DEFAULT '1900-01-01 00:00:00',"
                + "fecha_hora_ultima_modificacion TEXT NOT NULL DEFAULT '1900-01-01 00:00:00',"
                + "ultimo_usuario_en_modificar TEXT DEFAULT 'system'"
                + ");";
        
        // 2. Crear tabla principal de productos (Sin cambios)
        String sqlProductos = "CREATE TABLE IF NOT EXISTS productos ("
                + "id TEXT PRIMARY KEY,"
                + "nombre TEXT NOT NULL,"
                + "precio REAL NOT NULL,"
                + "cantidad INTEGER NOT NULL,"
                + "departamento TEXT NOT NULL,"
                + "almacen TEXT," // Almacena el ID del almacén (1, 2, 3, etc.)
                + "fecha_hora_creacion TEXT NOT NULL,"
                + "fecha_hora_ultima_modificacion TEXT NOT NULL,"
                + "ultimo_usuario_en_modificar TEXT"
                + ");";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlAlmacenes);
            stmt.execute(sqlProductos);
            System.out.println("DEBUG: Tablas 'almacenes' y 'productos' verificadas/creadas.");
        }
    }

    /**
     * Inserta los almacenes iniciales si no existen.
     */
    private static void populateAlmacenes() throws SQLException {
        String localizedNow = getLocalizedNow();
        String sql = "INSERT OR IGNORE INTO almacenes (id, nombre, fecha_hora_creacion, fecha_hora_ultima_modificacion, ultimo_usuario_en_modificar) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (Map.Entry<String, String> entry : ALMACENES_INICIALES.entrySet()) {
                pstmt.setString(1, entry.getKey());
                pstmt.setString(2, entry.getValue());
                pstmt.setString(3, localizedNow);
                pstmt.setString(4, localizedNow);
                pstmt.setString(5, "system_init");
                pstmt.executeUpdate();
            }
            System.out.println("DEBUG: Almacenes iniciales poblados (si no existían).");
        }
    }
    
    // ------------------------------------------------------------------
    //                       MÉTODOS DE CONEXIÓN
    // ------------------------------------------------------------------

    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL_CONEXION);
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos en: " + URL_CONEXION);
            e.printStackTrace();
        }
        return conn;
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
    
    // ------------------------------------------------------------------
    //                       MÉTODOS CRUD DE ALMACENES
    // ------------------------------------------------------------------

    /**
     * Inserta un nuevo almacén, asignando automáticamente el siguiente ID y campos de auditoría.
     */
    public static void insertarAlmacen(String nombre, String usuarioActual) throws SQLException {
        int nextId = getNextAlmacenId();
        String localizedNow = getLocalizedNow();
        String sql = "INSERT INTO almacenes (id, nombre, fecha_hora_creacion, fecha_hora_ultima_modificacion, ultimo_usuario_en_modificar) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, String.valueOf(nextId)); // El ID debe ser un String
            pstmt.setString(2, nombre);
            pstmt.setString(3, localizedNow); // Creación
            pstmt.setString(4, localizedNow); // Modificación
            pstmt.setString(5, usuarioActual);
            pstmt.executeUpdate();
            System.out.println("DEBUG: Almacén '" + nombre + "' (ID: " + nextId + ") insertado por: " + usuarioActual);
        }
    }

    /**
     * Actualiza el nombre de un almacén y sus campos de auditoría.
     */
    public static void actualizarAlmacen(int id, String nombre, String usuarioActual) throws SQLException {
        String localizedNow = getLocalizedNow();
        String sql = "UPDATE almacenes SET nombre = ?, fecha_hora_ultima_modificacion = ?, ultimo_usuario_en_modificar = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombre);
            pstmt.setString(2, localizedNow);
            pstmt.setString(3, usuarioActual);
            pstmt.setString(4, String.valueOf(id)); // El ID es Integer en el controller, String en la DB
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La actualización del almacén falló, ID no encontrado: " + id);
            }
            System.out.println("DEBUG: Almacén ID " + id + " actualizado por: " + usuarioActual);
        }
    }
    
    /**
     * Elimina un almacén por su ID.
     */
    public static void eliminarAlmacen(String id) throws SQLException {
        String sql = "DELETE FROM almacenes WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id); 
            pstmt.executeUpdate();
            System.out.println("DEBUG: Almacén ID " + id + " eliminado.");
        }
    }
    
    // ------------------------------------------------------------------
    //                       MÉTODOS DE CONSULTA DE ALMACENES (NUEVOS)
    // ------------------------------------------------------------------

    /**
     * Obtiene solo la lista de nombres de todos los almacenes para llenar ComboBoxes.
     */
    public static List<String> obtenerNombresAlmacenes() throws SQLException {
        List<String> nombres = new ArrayList<>();
        String sql = "SELECT nombre FROM almacenes ORDER BY nombre ASC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                nombres.add(rs.getString("nombre"));
            }
        }
        return nombres;
    }

    /**
     * Obtiene todos los almacenes de la base de datos. (Implementación solicitada)
     */
    public static ObservableList<Almacen> obtenerTodosLosAlmacenes() throws SQLException {
        // Al pasar 'null' como filtros, obtenerAlmacenesFiltrados ejecuta la consulta sin cláusula WHERE,
        // retornando todos los almacenes.
        return obtenerAlmacenesFiltrados(null);
    }
    
    /**
     * Obtiene una lista observable de Almacenes aplicando filtros (nombre o ID).
     */
    public static ObservableList<Almacen> obtenerAlmacenesFiltrados(List<String> filtros) throws SQLException {
        ObservableList<Almacen> listaAlmacenes = FXCollections.observableArrayList();
        
        String sqlBase = "SELECT id, nombre, fecha_hora_creacion, fecha_hora_ultima_modificacion, ultimo_usuario_en_modificar FROM almacenes";
        StringBuilder whereClause = new StringBuilder();
        List<String> params = new ArrayList<>();
        
        // Filtros esperados: [nombre (String), id (String)]
        if (filtros != null && filtros.size() >= 2) {
            String nombreFiltro = filtros.get(0);
            String idFiltro = filtros.get(1);
            
            if (nombreFiltro != null && !nombreFiltro.isEmpty()) {
                whereClause.append(" AND LOWER(nombre) = LOWER(?)"); 
                params.add(nombreFiltro); 
            }
            
            if (idFiltro != null && !idFiltro.isEmpty()) { 
                whereClause.append(" AND id = ?"); 
                params.add(idFiltro);
            }
        }
        
        String finalSql = sqlBase;
        if (whereClause.length() > 0) {
            finalSql += " WHERE 1=1 " + whereClause.toString(); 
        }
        finalSql += " ORDER BY id ASC"; 
        
        System.out.println("SQL Almacenes: " + finalSql);

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(finalSql)) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i)); 
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Almacen a = new Almacen(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("fecha_hora_creacion"),
                        rs.getString("fecha_hora_ultima_modificacion"),
                        rs.getString("ultimo_usuario_en_modificar")
                    );
                    listaAlmacenes.add(a);
                }
            }
        }
        
        return listaAlmacenes;
    }
    
    // ------------------------------------------------------------------
    //                       MÉTODOS DE CONSULTA DE PRODUCTOS
    // ------------------------------------------------------------------
    
    /**
     * Obtiene productos aplicando 8 filtros dinámicos.
     * Implementa JOIN para obtener el nombre del almacén.
     */
    public static ObservableList<Producto> obtenerProductosFiltrados(List<String> filtros) throws SQLException {
        ObservableList<Producto> listaProductos = FXCollections.observableArrayList();
        
        // Consulta base: Hacemos JOIN para traer el nombre del almacén
        String sqlBase = "SELECT p.id, p.nombre, p.precio, p.cantidad, p.departamento, " + 
                             "a.nombre AS almacen_nombre, " + // Traemos el nombre en lugar del ID
                             "p.fecha_hora_creacion, p.fecha_hora_ultima_modificacion, p.ultimo_usuario_en_modificar " +
                             "FROM productos p " +
                             "JOIN almacenes a ON p.almacen = a.id";
        
        StringBuilder whereClause = new StringBuilder();
        List<String> params = new ArrayList<>();
        
        // --- CONSTRUCCIÓN DINÁMICA DE LA CLÁUSULA WHERE ---
        
        if (filtros != null && filtros.size() >= 8) {
            
            // 0: Nombre (Búsqueda parcial LIKE)
            if (filtros.get(0) != null && !filtros.get(0).isEmpty()) {
                whereClause.append(" AND LOWER(p.nombre) LIKE LOWER(?)"); 
                params.add("%" + filtros.get(0) + "%"); 
            }
            
            // 1: Departamento (Filtro exacto)
            if (filtros.get(1) != null) {
                whereClause.append(" AND LOWER(p.departamento) = LOWER(?)");
                params.add(filtros.get(1));
            }
            
            // 2 & 3: Rango de Precio (Numérico)
            if (filtros.get(2) != null) { // precioMin
                whereClause.append(" AND p.precio >= ?");
                params.add(filtros.get(2));
            }
            if (filtros.get(3) != null) { // precioMax
                whereClause.append(" AND p.precio <= ?");
                params.add(filtros.get(3));
            }
            
            // 4 & 5: Rango de Cantidad (Numérico)
            if (filtros.get(4) != null) { // cantidadMin
                whereClause.append(" AND p.cantidad >= ?");
                params.add(filtros.get(4));
            }
            if (filtros.get(5) != null) { // cantidadMax
                whereClause.append(" AND p.cantidad <= ?");
                params.add(filtros.get(5));
            }
            
            // 6: ID Almacén (Filtro exacto, ahora filtra por ID de la tabla productos)
            if (filtros.get(6) != null) { 
                whereClause.append(" AND p.almacen = ?"); 
                params.add(filtros.get(6));
            }
            
            // 7: ID Producto (Exacto)
            if (filtros.get(7) != null && !filtros.get(7).isEmpty()) { 
                whereClause.append(" AND p.id = ?"); 
                params.add(filtros.get(7));
            }
        }
        
        String finalSql = sqlBase;
        if (whereClause.length() > 0) {
            finalSql += " WHERE 1=1 " + whereClause.toString(); 
        }
        finalSql += " ORDER BY p.nombre ASC"; 
        
        System.out.println("SQL Productos: " + finalSql);

        // --- EJECUCIÓN DE LA CONSULTA CON PREPARED STATEMENT ---
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(finalSql)) {
            
            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i)); 
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Producto p = new Producto(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        String.valueOf(rs.getDouble("precio")), 
                        String.valueOf(rs.getInt("cantidad")), 
                        rs.getString("departamento"),
                        rs.getString("almacen_nombre"), // AHORA LEE EL NOMBRE DE LA CIUDAD
                        rs.getString("fecha_hora_creacion"),
                        rs.getString("fecha_hora_ultima_modificacion"),
                        rs.getString("ultimo_usuario_en_modificar")
                    );
                    listaProductos.add(p);
                }
            }
        }
        
        return listaProductos;
    }
    
    public static boolean productoIdExiste(String id) throws SQLException {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT COUNT(*) FROM productos WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
    
    // ------------------------------------------------------------------
    //                       MÉTODOS CRUD DE PRODUCTOS 
    // ------------------------------------------------------------------

    /**
     * Agrega un nuevo producto. Usa hora localizada.
     */
    public static void agregarProducto(Producto producto) throws SQLException {
        
        String localizedNow = getLocalizedNow(); // Obtiene la hora localizada
        
        String sql = "INSERT INTO productos (id, nombre, precio, cantidad, departamento, almacen, fecha_hora_creacion, fecha_hora_ultima_modificacion, ultimo_usuario_en_modificar) " +
                      "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"; 
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, producto.getId());
            pstmt.setString(2, producto.getNombre());
            pstmt.setDouble(3, Double.parseDouble(producto.getPrecio()));
            pstmt.setInt(4, Integer.parseInt(producto.getCantidad()));
            pstmt.setString(5, producto.getDepartamento());
            pstmt.setString(6, producto.getAlmacen()); // Contiene el ID del almacén
            
            pstmt.setString(7, localizedNow); // Hora de creación
            pstmt.setString(8, localizedNow); // Hora de última modificación
            pstmt.setString(9, SessionContext.getCurrentUserName()); 
            
            pstmt.executeUpdate();
            System.out.println("DEBUG: Producto agregado por: " + SessionContext.getCurrentUserName());
        } catch (NumberFormatException e) {
            throw new SQLException("Error de formato numérico en Precio o Cantidad: " + e.getMessage());
        }
    }

    /**
     * Actualiza un producto existente. Usa hora localizada.
     */
    public static void actualizarProducto(Producto producto) throws SQLException {
        
        String localizedNow = getLocalizedNow(); // Obtiene la hora localizada

        String sql = "UPDATE productos SET nombre = ?, precio = ?, cantidad = ?, departamento = ?, almacen = ?, " +
                      "fecha_hora_ultima_modificacion = ?, ultimo_usuario_en_modificar = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, producto.getNombre());
            pstmt.setDouble(2, Double.parseDouble(producto.getPrecio()));
            pstmt.setInt(3, Integer.parseInt(producto.getCantidad()));
            pstmt.setString(4, producto.getDepartamento());
            pstmt.setString(5, producto.getAlmacen()); // Contiene el ID del almacén
            
            pstmt.setString(6, localizedNow); // Hora de última modificación
            pstmt.setString(7, SessionContext.getCurrentUserName());
            pstmt.setString(8, producto.getId());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("La actualización falló, ID no encontrado: " + producto.getId());
            }
            System.out.println("DEBUG: Producto actualizado por: " + SessionContext.getCurrentUserName());
        } catch (NumberFormatException e) {
            throw new SQLException("Error de formato numérico en Precio o Cantidad: " + e.getMessage());
        }
    }
    
    /**
     * Elimina un producto por su ID.
     */
    public static void eliminarProducto(String id) throws SQLException {
        String sql = "DELETE FROM productos WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, id); 
            pstmt.executeUpdate();
        }
    }
}