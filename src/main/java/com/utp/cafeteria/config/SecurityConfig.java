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
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> {})
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                // Monitoreo: estado e info publicos; el resto (metricas) solo ADMIN
                .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categories", "/api/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/subcategories", "/api/subcategories/**").permitAll()
                .requestMatchers("/api/payments/webhook").permitAll()
                .requestMatchers("/api/reports/**").hasRole("ADMIN")
                .requestMatchers("/api/products/**").hasRole("ADMIN")
                .requestMatchers("/api/categories/**").hasRole("ADMIN")
                .requestMatchers("/api/subcategories/**").hasRole("ADMIN")
                .requestMatchers("/api/orders/**").hasAnyRole("ADMIN", "CAJA", "USUARIO")
                .requestMatchers("/api/cart/**").hasRole("USUARIO")
                .requestMatchers("/api/payments/**").hasRole("USUARIO")
                .anyRequest().authenticated()
            )
            .headers(headers -> headers
                // Evita clickjacking: la app no puede incrustarse en un <iframe>.
                .frameOptions(frame -> frame.deny())
                // Evita que el navegador "adivine" tipos MIME (X-Content-Type-Options: nosniff).
                .contentTypeOptions(withDefaults())
                // Fuerza HTTPS en navegadores compatibles una vez servido por HTTPS.
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000))
                // Limita la información de referer que se envía a otros orígenes.
                .referrerPolicy(ref -> ref
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
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