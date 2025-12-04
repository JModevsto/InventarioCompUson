package mx.unison;

import org.mindrot.jbcrypt.BCrypt;

public class Seguridad {

    // 1. Generar Hash: Se usa una sola vez para guardar la contraseña en la BD.
    public static String generarHash(String password) {
        // gensalt() genera una "sal" aleatoria para mayor seguridad.
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // 2. Verificar Contraseña (para el login):
    // Retorna true si el texto plano coincide con el hash guardado.
    public static boolean verificarPassword(String passwordTextoPlano, String hashGuardado) {
        return BCrypt.checkpw(passwordTextoPlano, hashGuardado);
    }
}