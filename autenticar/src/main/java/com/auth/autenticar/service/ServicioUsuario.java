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
       // System.out.println("=== SERVICIO - DEBUG AUTENTICACIÓN ===");

        // Validar inputs
        if (password == null || password.trim().isEmpty()) {
            System.out.println("ERROR: Password vacía o nula");
            return false;
        }

        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            System.out.println("ERROR: Nombre de usuario vacío o nulo");
            return false;
        }

     

        if (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$")) {
          
            return false;
        }

        ModeloUsuario usuarioOpt = repositorioUsuario.findByCorreo(nombreUsuario);

        // usuarioOpt.getContrasena() -> clave de base de datos
        if (usuarioOpt == null) {
            
            return false;
        }
   

        // Verificar que el hash es válido
        String hashBD = usuarioOpt.getContrasena();
        if (hashBD == null || hashBD.isEmpty()) {
            // System.out.println("ERROR: Hash en BD está vacío o es nulo");
            return false;
        }

        // Verificar formato BCrypt
        if (!hashBD.startsWith("$2a$") && !hashBD.startsWith("$2b$") && !hashBD.startsWith("$2y$")) {
          
            return false;
        }

        try {
            // Realizar comparación
            boolean resultado = BCrypt.checkpw(password, hashBD);
           
            return resultado;
        } catch (Exception e) {
            System.out.println("ERROR en BCrypt.checkpw: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}
