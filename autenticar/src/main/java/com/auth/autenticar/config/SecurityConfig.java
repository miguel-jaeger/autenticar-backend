package com.auth.autenticar.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        @Autowired
        private JwtRequestFilter jwtRequestFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                // Configurar CORS CORRECTAMENTE con la configuración
                http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

                http.headers()
                                .httpStrictTransportSecurity().includeSubDomains(true).maxAgeInSeconds(31536000);
                http.headers()
                                .addHeaderWriter(new StaticHeadersWriter("X-Content-Security-Policy",
                                                "default-src 'self'"))
                                .addHeaderWriter(new StaticHeadersWriter("X-WebKit-CSP", "default-src 'self'"));

                http
                                // 1. Deshabilitar CSRF (necesario para APIs REST Stateless)
                                .csrf(AbstractHttpConfigurer::disable)

                                // 2. Definir las reglas de Autorización (Quién accede a dónde)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Permitir
                                                                                                        // preflight
                                                                                                        // CORS
                                                .requestMatchers("/api/usuarios/autenticar").permitAll() // Login
                                                                                                         // público
                                                .requestMatchers("/api/usuarios/registrar").permitAll() // Registro
                                                                                                        // público
                                                .requestMatchers("/api/actualizar-contrasena").permitAll()
                                                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                                                .anyRequest().authenticated())

                                // 3. CRUCIAL: Configurar el sistema como Stateless (sin sesiones)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                                // 4. Añadir nuestro filtro JWT ANTES del filtro de autenticación estándar
                                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        // NUEVO: Configuración de CORS para Spring Security
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(Arrays.asList(
                                "http://localhost:5173",
                                "http://localhost:4002",
                                "https://autenticar-frontend.onrender.com"));

                configuration.setAllowedMethods(Arrays.asList(
                                "GET", "POST", "PUT", "DELETE", "OPTIONS"));

                configuration.setAllowedHeaders(Arrays.asList("*"));
                configuration.setExposedHeaders(Arrays.asList("Authorization"));
                configuration.setAllowCredentials(true);
                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}