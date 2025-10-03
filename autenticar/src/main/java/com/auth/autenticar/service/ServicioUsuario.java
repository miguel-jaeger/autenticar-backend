package com.auth.autenticar.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auth.autenticar.model.ModeloUsuario;
import com.auth.autenticar.repository.IRepositorioUsuario;

import java.util.Optional;

import org.mindrot.jbcrypt.BCrypt;

@Service
public class ServicioUsuario {

    @Autowired
    IRepositorioUsuario repositorioUsuario;

    public ArrayList<ModeloUsuario> listarUsuarios() {
        return (ArrayList<ModeloUsuario>) repositorioUsuario.findAll();
    }

    public ModeloUsuario guardarUsuario(ModeloUsuario usuario) {
        return repositorioUsuario.save(usuario);
    }

    public ModeloUsuario actualizarUsuario(ModeloUsuario usuario) {
        return repositorioUsuario.save(usuario);
    }

    public void eliminarUsuario(ModeloUsuario usuario) {
        repositorioUsuario.deleteById(usuario.getIdPersona());
    }

    public ModeloUsuario obtenerUsuarioPorId(Long id) {
        return repositorioUsuario.findById(id).orElse(null);
    }

    public ModeloUsuario obtenerPorCorreo(String correo) {
        return repositorioUsuario.findByCorreo(correo);
    }

    public boolean autenticarUsuario(String nombreUsuario, String password) {
        System.out.println("=== SERVICIO - DEBUG AUTENTICACIÓN ===");

        // Validar inputs
        if (password == null || password.trim().isEmpty()) {
            System.out.println("ERROR: Password vacía o nula");
            return false;
        }

        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            System.out.println("ERROR: Nombre de usuario vacío o nulo");
            return false;
        }

        // System.out.println("Password recibida en servicio: [" + password + "]");
        // System.out.println("Longitud password: " + password.length());

        if (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$")) {
            /*
             * System.out.println(" ERROR CRÍTICO: La contraseña recibida YA ES UN HASH!");
             * System.out.
             * println(" El frontend/cliente está enviando el hash en lugar de texto plano"
             * );
             * System.out.println(" Debes enviar la contraseña SIN hashear");
             */
            return false;
        }

        ModeloUsuario usuarioOpt = repositorioUsuario.findByCorreo(nombreUsuario);

        // usuarioOpt.getContrasena() -> clave de base de datos
        if (usuarioOpt == null) {
            // System.out.println("ERROR: Usuario no encontrado con correo: [" +
            // nombreUsuario + "]");
            return false;
        }
        /*
         * System.out.println("Usuario encontrado: " + usuarioOpt.getCorreo());
         * 
         * 
         * System.out.println("Hash almacenado en BD: [" + hashBD + "]");
         * System.out.println("Longitud del hash: " + (hashBD != null ? hashBD.length()
         * : "NULL"));
         */

        // Verificar que el hash es válido
        String hashBD = usuarioOpt.getContrasena();
        if (hashBD == null || hashBD.isEmpty()) {
            // System.out.println("ERROR: Hash en BD está vacío o es nulo");
            return false;
        }

        // Verificar formato BCrypt
        if (!hashBD.startsWith("$2a$") && !hashBD.startsWith("$2b$") && !hashBD.startsWith("$2y$")) {
            // System.out.println("ERROR: El hash en BD NO es un hash BCrypt válido");
            // System.out.println("El hash debe comenzar con $2a$, $2b$ o $2y$");
            return false;
        }

        try {
            // Realizar comparación
            boolean resultado = BCrypt.checkpw(password, hashBD);
            /*
             * System.out.println("Resultado BCrypt.checkpw: " + resultado);
             * System.out.println("======================================");
             */
            return resultado;
        } catch (Exception e) {
            System.out.println("ERROR en BCrypt.checkpw: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}
