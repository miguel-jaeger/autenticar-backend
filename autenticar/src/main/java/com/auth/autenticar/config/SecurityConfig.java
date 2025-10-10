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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        @Autowired
        private JwtRequestFilter jwtRequestFilter;

        @SuppressWarnings("removal")
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsSource)
                        throws Exception {
                // Usar explícitamente la configuración CORS (la que registraste en CorsConfig)
                http.cors(cors -> cors.configurationSource(corsSource));

                http.headers()
                                .httpStrictTransportSecurity().includeSubDomains(true).maxAgeInSeconds(31536000);
                http.headers()
                                .addHeaderWriter(new StaticHeadersWriter("X-Content-Security-Policy",
                                                "default-src 'self'"))
                                .addHeaderWriter(new StaticHeadersWriter("X-WebKit-CSP", "default-src 'self'"));

                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers("/api/usuarios/**").permitAll()
                                                .requestMatchers("/api/v1/usuarios/**").permitAll()
                                                .requestMatchers("/api/actualizar-contrasena").permitAll()
                                                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
