package com.auth.autenticar.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth.autenticar.model.ModeloUsuario;
import com.auth.autenticar.service.ServicioUsuario;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.ui.Model;

import org.mindrot.jbcrypt.BCrypt;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/usuarios")
public class ControladorUsuario {

    @Autowired
    private ServicioUsuario servicioUsuario;

    private final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Listar
    @GetMapping
    public ArrayList<ModeloUsuario> listarUsuarios() {
        return servicioUsuario.listarUsuarios();
    }

    // Adicionar
    @PostMapping
    public ModeloUsuario salvarUsuario(@RequestBody ModeloUsuario usuario) {
        usuario.setContrasena(this.claveEncriptada(usuario.getContrasena()));
        return this.servicioUsuario.guardarUsuario(usuario);
    }

    @PutMapping
    public ModeloUsuario actualizarUsuario(@RequestBody ModeloUsuario usuario) {
        // usuario.setContrasena(this.claveEncriptada(usuario.getContrasena()));
        return this.servicioUsuario.actualizarUsuario(usuario);
    }

    @DeleteMapping
    public void eliminarUsuario(@RequestBody ModeloUsuario usuario) {
        this.servicioUsuario.eliminarUsuario(usuario);
    }

    public String claveEncriptada(String clave) {
        return BCrypt.hashpw(clave, BCrypt.gensalt());
    }

    /*
     * @GetMapping("usuarios/autenticar")
     * public String mostrarAutenticarUsuario(Model modelo) {
     * ModeloUsuario usuario = new ModeloUsuario();
     * modelo.addAttribute("usuario", usuario);
     * return "autenticarUsuario";
     * }
     */

    @PostMapping("/autenticarUsuario")
    public ResponseEntity<Map<String, String>> autenticarUsuario(@RequestBody ModeloUsuario usuario) {
        Map<String, String> response = new HashMap<>();
        System.out.println("usuario.getCorreo()" + usuario.getCorreo());
        System.out.println("usuario.getContrasena()" + usuario.getContrasena());
        boolean valida = servicioUsuario.autenticarUsuario(usuario.getCorreo(), usuario.getContrasena());
        System.out.println("valida" + valida);
        if (valida) {
            String token = Jwts.builder()
                    .setSubject(usuario.getCorreo())
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

    /*
     * @GetMapping("/autenticar404")
     * public String autenticarError(Model modelo) {
     * return "autenticar404";
     * }
     */

}
