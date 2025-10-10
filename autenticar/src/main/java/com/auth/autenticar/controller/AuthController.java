/*package com.auth.autenticar.controller;

import com.auth.autenticar.model.ModeloUsuario;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys; // Importación necesaria
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey; // Importación necesaria

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    // Genera una clave segura para Base64 directamente
    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Hardcoded credentials for demonstration
    private String currentPassword = "1qazxsw2";
    private String currentUsername = "admin";


    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody ModeloUsuario loginRequest) {
        Map<String, String> response = new HashMap<>();

        if (currentUsername.equals(loginRequest.getNombre()) && currentPassword.equals(loginRequest.getContrasena())) {
            
            String token = Jwts.builder()
                .setSubject(loginRequest.getNombre())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(SECRET_KEY) // El método ya acepta la clave generada
                .compact();

            response.put("token", token);
            return ResponseEntity.ok(response);
        }

        response.put("error", "Credenciales inválidas");
        // Error: código de estado 401 Unauthorized
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
}*/