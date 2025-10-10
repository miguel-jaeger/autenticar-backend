package com.auth.autenticar.controller;

import com.auth.autenticar.model.ModeloUsuarioFireBase;
import com.auth.autenticar.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // DTO simple para login (si ya tienes uno en otro package, importa ese en su lugar)
    public static class LoginRequest {
        private String correo;
        private String contrasena;

        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
        public String getContrasena() { return contrasena; }
        public void setContrasena(String contrasena) { this.contrasena = contrasena; }
    }

    /**
     * Registrar nuevo usuario
     * POST /api/v1/usuarios/registro
     */
    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody ModeloUsuarioFireBase usuario) {
        try {
            ModeloUsuarioFireBase nuevoUsuario = usuarioService.registrarUsuario(usuario);

            // No devolver la contraseña en la respuesta
            if (nuevoUsuario != null) nuevoUsuario.setContrasena(null);

            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);

        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Login de usuario
     * POST /api/v1/usuarios/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Optional<ModeloUsuarioFireBase> usuarioOpt = usuarioService.autenticar(
                loginRequest.getCorreo(),
                loginRequest.getContrasena()
            );

            if (usuarioOpt.isPresent()) {
                ModeloUsuarioFireBase usuario = usuarioOpt.get();

                // No devolver la contraseña
                usuario.setContrasena(null);

                Map<String, Object> response = new HashMap<>();
                response.put("mensaje", "Login exitoso");
                response.put("usuario", usuario);
                return ResponseEntity.ok(response);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Credenciales inválidas");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al autenticar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener usuario por ID
     * GET /api/v1/usuarios/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuarioPorId(@PathVariable String id) {
        try {
            Optional<ModeloUsuarioFireBase> usuarioOpt = usuarioService.obtenerUsuarioPorId(id);

            if (usuarioOpt.isPresent()) {
                ModeloUsuarioFireBase usuario = usuarioOpt.get();
                usuario.setContrasena(null);
                return ResponseEntity.ok(usuario);
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al buscar usuario: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Obtener todos los usuarios
     * GET /api/v1/usuarios
     */
    @GetMapping
    public ResponseEntity<?> obtenerTodosLosUsuarios() {
        try {
            List<ModeloUsuarioFireBase> usuarios = usuarioService.obtenerTodosLosUsuarios();
            usuarios.forEach(u -> { if (u != null) u.setContrasena(null); });
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al obtener usuarios: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
