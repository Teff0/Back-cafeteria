package com.utp.cafeteria.security;

import com.utp.cafeteria.config.SupabaseConfig;
import com.utp.cafeteria.service.SupabaseAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SupabaseAuthFilter extends OncePerRequestFilter {

    private final SupabaseAuthService supabaseAuthService;
    private final SupabaseConfig supabaseConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            Map<String, Object> user = supabaseAuthService.getUser(token);
            
            if (user != null && user.get("id") != null) {
                String email = (String) user.get("email");
                @SuppressWarnings("unchecked")
                Map<String, Object> userMetadata = (Map<String, Object>) user.get("user_metadata");
                String rol = userMetadata != null && userMetadata.containsKey("rol") 
                    ? (String) userMetadata.get("rol") 
                    : "ESTUDIANTE";

                List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + rol)
                );

                UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                    email, "", authorities
                );

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            logger.error("Error al verificar token: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}