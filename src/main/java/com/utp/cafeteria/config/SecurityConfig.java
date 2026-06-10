package com.utp.cafeteria.config;

import com.utp.cafeteria.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .authorizeHttpRequests(auth -> auth
                // Preflight CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Imágenes subidas (acceso público para mostrarlas)
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                // Auth
                .requestMatchers("/api/auth/**").permitAll()
                // WebSocket
                .requestMatchers("/ws/**").permitAll()
                // Productos - lectura pública
                .requestMatchers(HttpMethod.GET, "/api/products").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                // Categorías - lectura pública
                .requestMatchers(HttpMethod.GET, "/api/categories").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                // Subcategorías - lectura pública
                .requestMatchers(HttpMethod.GET, "/api/subcategories").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/subcategories/**").permitAll()
                // Reseñas - lectura pública
                .requestMatchers(HttpMethod.GET, "/api/reviews/**").permitAll()
                // Promos - validación y activas públicas
                .requestMatchers(HttpMethod.GET, "/api/promos/activas").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/promos/validar/**").permitAll()
                // Config cafetería - lectura pública
                .requestMatchers(HttpMethod.GET, "/api/config").permitAll()
                // Pagos webhook
                .requestMatchers("/api/payments/webhook").permitAll()
                // Admin
                .requestMatchers("/api/reports/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/subcategories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/subcategories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/subcategories/**").hasRole("ADMIN")
                .requestMatchers("/api/promos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/config").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/config/**").hasRole("ADMIN")
                // Caja
                .requestMatchers("/api/orders/**").hasAnyRole("ADMIN", "CAJA", "USUARIO")
                // Usuario autenticado
                .requestMatchers("/api/cart/**").hasRole("USUARIO")
                .requestMatchers("/api/payments/**").hasRole("USUARIO")
                .requestMatchers("/api/notifications/**").authenticated()
                .requestMatchers("/api/reviews/**").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
