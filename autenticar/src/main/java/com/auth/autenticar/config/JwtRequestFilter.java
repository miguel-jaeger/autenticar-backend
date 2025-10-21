package com.auth.autenticar.config;

import com.auth.autenticar.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    // NUEVO: Método para identificar rutas públicas
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // No filtrar rutas públicas
        boolean isPublicPath = path.equals("/api/usuarios/autenticar") ||
                path.equals("/api/usuarios/registrar") ||
                path.equals("/api/actualizar-contrasena");

        // No filtrar peticiones OPTIONS (CORS preflight)
        boolean isOptionsRequest = "OPTIONS".equalsIgnoreCase(method);

        return isPublicPath || isOptionsRequest;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. Obtener la cabecera 'Authorization'
            final String authorizationHeader = request.getHeader("Authorization");

            String username = null;
            String jwt = null;

            // 2. Verificar si la cabecera existe y comienza con "Bearer "
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7); // Extraer el token (después de "Bearer ")

                // 3. Intentar obtener el nombre de usuario del token
                username = jwtUtil.getUsernameFromToken(jwt);
            }

            // 4. Si tenemos el username y nadie ha autenticado aún esta petición:
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 5. Validar el token y, si es válido, cargar los permisos (roles)
                if (jwtUtil.validateToken(jwt)) {

                    List<String> roles = jwtUtil.getRolesFromToken(jwt);

                    // Obtener roles del token (RBAC)
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    // Crear el Objeto de Autenticación
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);

                    // 6. Establecer el contexto de seguridad (autenticar la petición)
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            logger.warn("Error procesando JWT: " + e.getMessage());
            // No lanzar excepción para no bloquear la cadena
        }

        // Continuar cadena de filtros siempre, incluso si no hay token o falló
        // validación
        filterChain.doFilter(request, response);
    }
}
