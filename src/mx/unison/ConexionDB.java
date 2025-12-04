package mx.unison;

import java.sql.*; 

public class ConexionDB {

    // Ruta donde se encuentra tu archivo de base de datos
    private static final String URL_DB = "jdbc:sqlite:./mi_base_de_datos.sqbpro"; 
    // Si la BD está en la misma carpeta que el proyecto, usa esta ruta relativa.

    public static boolean verificarCredenciales(String usuario, String password) {
        // La consulta SQL busca el hash de la contraseña para ese usuario.
        String sql = "SELECT password_hash FROM usuarios WHERE nombre_usuario = ?";

        try (Connection conn = DriverManager.getConnection(URL_DB);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // Se encontró el usuario, ahora verificamos la contraseña hasheada.
                String hashGuardado = rs.getString("password_hash");

                if (Seguridad.verificarPassword(password, hashGuardado)) {
                    // ¡Login exitoso! Ahora actualizamos la fecha de último inicio de sesión.
                    actualizarFechaLogin(usuario);
                    return true;
                }
            }
            return false; // Usuario no encontrado o contraseña incorrecta.

        } catch (SQLException e) {
            System.err.println("Error de conexión o consulta: " + e.getMessage());
            return false;
        }
    }

    // Método privado para actualizar el campo ultimo_login
    private static void actualizarFechaLogin(String usuario) {
        String sql = "UPDATE usuarios SET ultimo_login = DATETIME('now','localtime') WHERE nombre_usuario = ?";
        try (Connection conn = DriverManager.getConnection(URL_DB);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al actualizar la fecha de login: " + e.getMessage());
        }
    }
}