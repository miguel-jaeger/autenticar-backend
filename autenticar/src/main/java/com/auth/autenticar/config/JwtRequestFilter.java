package com.auth.autenticar.config;



import com.auth.autenticar.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Keep this line
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Keep this line
import org.springframework.security.core.context.SecurityContextHolder; // Keep this line
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @SuppressWarnings("null")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Obtener la cabecera 'Authorization'
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 2. Verificar si la cabecera existe y comienza con "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Extraer el token (después de "Bearer ")
            
            // 3. Intentar obtener el nombre de usuario del token (puede fallar si la firma es inválida)
            try {
                username = jwtUtil.getUsernameFromToken(jwt);
            } catch (Exception e) {
                logger.warn("JWT inválido o expirado: " + e.getMessage());
            }
        }

        // 4. Si tenemos el username y nadie ha autenticado aún esta petición:
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // 5. Validar el token y, si es válido, cargar los permisos (roles)
            if (jwtUtil.validateToken(jwt)) {
                
                List<String> roles = jwtUtil.getRolesFromToken(jwt);

                // Obtener roles del token (EJEMPLO ABAC/RBAC)
                List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

                // Crear el Objeto de Autenticación con el usuario y sus permisos
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username, null, authorities);
                
                // 🔑 6. Establecer el contexto de seguridad (¡Esto autentica la petición!)
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response); // Continúa al siguiente filtro
    }
}
