package com.auth.autenticar.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.autenticar.model.ModeloUsuario;
import com.auth.autenticar.service.ServicioUsuario;
import com.auth.autenticar.util.JwtUtil;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.ui.Model;

import org.mindrot.jbcrypt.BCrypt;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class ControladorUsuario {

    @Autowired
    private ServicioUsuario servicioUsuario;
     @SuppressWarnings("unused")
    private JwtUtil jwtUtil;

    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        // 1. ENDPOINT PÚBLICO: Genera el token si las credenciales son correctas.
    @PostMapping("/auth/login")
    public ResponseEntity<String> login(@RequestBody ModeloUsuario usuario) {
        
        // **SIMULACIÓN** de verificación de credenciales en la base de datos
        if ("admin".equals(usuario.getCorreo()) && "pass".equals(usuario.getContrasena()) && "pass".equals(usuario.getContrasena())) {
            
            //  Generar JWT con el Rol "ADMIN"
            String token = jwtUtil.generateToken(usuario.getCorreo(), List.of("ADMIN"));
            
            // Devolver el token al cliente (Front-end)
            return ResponseEntity.ok("{\"token\": \"" + token + "\"}");
        }
        return ResponseEntity.status(401).body("Credenciales inválidas");
    }

    // Listar
    @GetMapping
     @PreAuthorize("hasAuthority('ADMIN')") 
    public ArrayList<ModeloUsuario> listarUsuarios() {
        return servicioUsuario.listarUsuarios();
    }

    // Adicionar usuario
    @PostMapping
   // @PreAuthorize("hasAuthority('ADMIN')") // Autorización usando ABAC/RBAC
    public ResponseEntity<Map<String, String>> salvarUsuario(@RequestBody ModeloUsuario usuario) {
        Map<String, String> response = new HashMap<>();

        // TRIM desde el inicio
        String passwordOriginal = usuario.getContrasena().trim();

        if (isBCryptHash(passwordOriginal)) {
            response.put("error", "Password ya hasheado");
            return ResponseEntity.badRequest().body(response);
        }

        // Hashear la contraseña limpia
        String passwordHasheada = this.claveEncriptada(passwordOriginal);
        usuario.setContrasena(passwordHasheada);

        ModeloUsuario guardado = this.servicioUsuario.guardarUsuario(usuario);

        // Verificación inmediata
        ModeloUsuario verificacion = this.servicioUsuario.obtenerPorCorreo(guardado.getCorreo());
        boolean pruebaInmediata = BCrypt.checkpw(passwordOriginal, verificacion.getContrasena());

        guardado.setContrasena(null);

        response.put("mensaje", "Usuario creado exitosamente");
        response.put("correo", guardado.getCorreo());
        response.put("pruebaInmediata", String.valueOf(pruebaInmediata));

        if (!pruebaInmediata) {
            response.put("advertencia", "La contraseña NO se guardó correctamente");
        }

        return ResponseEntity.ok(response);
    }

    // Actualizar usuario
    @PutMapping
    public ResponseEntity<?> actualizarUsuario(@RequestBody ModeloUsuario usuario) {
        try {
           
            if (usuario.getIdPersona() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El ID es requerido"));
            }

            ModeloUsuario actualizado = this.servicioUsuario.actualizarUsuario(usuario);
            actualizado.setContrasena(null); // No devolver el hash           
            return ResponseEntity.ok(actualizado);

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }   

    }

    // Eliminar usuario
    @DeleteMapping
    public void eliminarUsuario(@RequestBody ModeloUsuario usuario) {
        this.servicioUsuario.eliminarUsuario(usuario);
    }

    // ENDPOINT: Actualizar contraseña de un usuario
    @PostMapping("/actualizar-contrasena")
    public ResponseEntity<Map<String, String>> actualizarPassword(@RequestBody Map<String, String> datos) {
        Map<String, String> response = new HashMap<>();

        String correo = datos.get("correo");
        String nuevaPassword = datos.get("password");

        if (nuevaPassword == null) {
            nuevaPassword = datos.get("contrasena");
        }

        if (correo == null || nuevaPassword == null) {
            response.put("error", "Faltan parámetros");
            return ResponseEntity.badRequest().body(response);
        }

        ModeloUsuario usuario = servicioUsuario.obtenerPorCorreo(correo);

        if (usuario == null) {
            response.put("error", "Usuario no encontrado");
            return ResponseEntity.notFound().build();
        }

        // Actualizar con hash nuevo
        usuario.setContrasena(claveEncriptada(nuevaPassword));
        servicioUsuario.actualizarUsuario(usuario);

        response.put("mensaje", "Contraseña actualizada exitosamente");
        response.put("correo", correo);
        response.put("nuevoHash", usuario.getContrasena());

        return ResponseEntity.ok(response);
    }

    public String claveEncriptada(String clave) {
        return BCrypt.hashpw(clave, BCrypt.gensalt());
    }

    // MÉTODO DE AUTENTICACIÓN CORREGIDO
    @PostMapping("/autenticar")
    public ResponseEntity<Map<String, String>> autenticarUsuario(@RequestBody ModeloUsuario usuario) {
        Map<String, String> response = new HashMap<>();

        if (usuario.getCorreo() == null || usuario.getContrasena() == null) {
            response.put("error", "Correo o contraseña vacíos");
            return ResponseEntity.badRequest().body(response);
        }

        String correo = usuario.getCorreo().trim();
        String contrasena = usuario.getContrasena().trim();
        boolean valida = servicioUsuario.autenticarUsuario(correo, contrasena);

        if (valida) {
            String token = Jwts.builder()
                    .setSubject(correo)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(SECRET_KEY)
                    .compact();

            response.put("token", token);
            response.put("mensaje", "Autenticación exitosa");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Credenciales inválidas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // ENDPOINT PARA VERIFICAR HASH DE UN USUARIO
    @GetMapping("/verificar-hash/{correo}")
    public ResponseEntity<Map<String, String>> verificarHash(@PathVariable String correo) {
        Map<String, String> response = new HashMap<>();

        ModeloUsuario usuario = servicioUsuario.obtenerPorCorreo(correo);

        if (usuario == null) {
            response.put("error", "Usuario no encontrado");
            return ResponseEntity.notFound().build();
        }

        String hash = usuario.getContrasena();
        boolean esBCrypt = isBCryptHash(hash);

        response.put("correo", correo);
        response.put("hash", hash);
        response.put("esBCryptValido", String.valueOf(esBCrypt));
        response.put("longitudHash", String.valueOf(hash != null ? hash.length() : 0));

        return ResponseEntity.ok(response);
    }

    // Método auxiliar para verificar si es un hash BCrypt
    private static boolean isBCryptHash(String s) {
        return s != null && (s.startsWith("$2a$") || s.startsWith("$2b$") || s.startsWith("$2y$"));
    }

    @PostMapping("/test-bcrypt")
    public ResponseEntity<Map<String, Object>> testBCrypt(@RequestBody Map<String, String> datos) {
        Map<String, Object> response = new HashMap<>();

        try {
            String correo = datos.get("correo");
            String passwordPlainText = datos.get("contrasena");

            System.out.println("=== TEST BCRYPT ===");
            System.out.println("Correo: " + correo);
            System.out.println("Password: " + passwordPlainText);

            if (correo == null || passwordPlainText == null) {
                response.put("error", "Faltan parámetros correo o password");
                return ResponseEntity.badRequest().body(response);
            }

            ModeloUsuario usuario = servicioUsuario.obtenerPorCorreo(correo);

            if (usuario == null) {
                response.put("error", "Usuario no encontrado");
                return ResponseEntity.notFound().build();
            }

            String hashBD = usuario.getContrasena();

            System.out.println("Hash en BD: " + hashBD);

            // Prueba directa
            boolean resultado = BCrypt.checkpw(passwordPlainText, hashBD);

            System.out.println("Resultado: " + resultado);
            System.out.println("==================");

            response.put("correo", correo);
            response.put("passwordRecibida", passwordPlainText);
            response.put("hashEnBD", hashBD);
            response.put("resultadoBCrypt", resultado);
            response.put("longitudPassword", passwordPlainText.length());
            response.put("longitudHash", hashBD.length());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getClass().getName());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
