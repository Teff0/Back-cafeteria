package com.utp.cafeteria.service;

import com.utp.cafeteria.config.SupabaseConfig;
import com.utp.cafeteria.dto.AuthResponse;
import com.utp.cafeteria.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SupabaseAuthService {

    private final SupabaseConfig supabaseConfig;
    private final WebClient.Builder webClientBuilder;

    public AuthResponse signUp(String email, String password, String nombre, String rol) {
        WebClient client = webClientBuilder.build();

        Map<String, Object> body = Map.of(
            "email", email,
            "password", password,
            "data", Map.of("nombre", nombre, "rol", rol)
        );

        String url = supabaseConfig.getUrl() + "/auth/v1/signup";

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = client.post()
                .uri(url)
                .header("apikey", supabaseConfig.getKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response == null || response.containsKey("error_msg")) {
                String errorMsg = response != null ? (String) response.get("error_msg") : "Error al registrar";
                throw new BadRequestException(errorMsg);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) response.get("user");
            String userId = user != null ? (String) user.get("id") : null;

            if (userId != null) {
                createOrUpdateUserInDb(userId, email, nombre, rol);
            }

            String accessToken = (String) response.get("access_token");
            return AuthResponse.of(accessToken, email, rol, nombre);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Error en registro: " + e.getMessage());
        }
    }

    public AuthResponse signIn(String email, String password) {
        WebClient client = webClientBuilder.build();

        Map<String, Object> body = Map.of(
            "email", email,
            "password", password
        );

        String url = supabaseConfig.getUrl() + "/auth/v1/token?grant_type=password";

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = client.post()
                .uri(url)
                .header("apikey", supabaseConfig.getKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response == null || response.containsKey("error")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> error = (Map<String, Object>) response.get("error");
                String errorMsg = error != null ? (String) error.get("description") : "Credenciales inválidas";
                throw new UnauthorizedException(errorMsg);
            }

            String accessToken = (String) response.get("access_token");
            @SuppressWarnings("unchecked")
            Map<String, Object> user = (Map<String, Object>) response.get("user");
            String nombre = user != null ? (String) user.get("email") : email;
            
            @SuppressWarnings("unchecked")
            Map<String, Object> appMetadata = user != null ? (Map<String, Object>) user.get("app_metadata") : null;
            String rol = appMetadata != null && appMetadata.containsKey("rol") 
                ? (String) appMetadata.get("rol") 
                : "ESTUDIANTE";

            return AuthResponse.of(accessToken, email, rol, nombre);
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Credenciales inválidas");
        }
    }

    public Map<String, Object> getUser(String token) {
        WebClient client = webClientBuilder.build();

        String url = supabaseConfig.getUrl() + "/auth/v1/user";

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = client.get()
                .uri(url)
                .header("apikey", supabaseConfig.getKey())
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            return response;
        } catch (Exception e) {
            throw new UnauthorizedException("Token inválido");
        }
    }

    private void createOrUpdateUserInDb(String supabaseUserId, String email, String nombre, String rol) {
        WebClient client = webClientBuilder.build();

        String url = supabaseConfig.getUrl() + "/rest/v1/rpc/upsert_user";

        Map<String, Object> body = Map.of(
            "p_supabase_user_id", supabaseUserId,
            "p_email", email,
            "p_nombre", nombre,
            "p_rol", rol
        );

        try {
            client.post()
                .uri(url)
                .header("apikey", supabaseConfig.getServiceKey())
                .header("Authorization", "Bearer " + supabaseConfig.getServiceKey())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        } catch (Exception e) {
            // Log error but don't fail
        }
    }
}