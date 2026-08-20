package com.echovault.config;

import com.echovault.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 1. Static Assets & Frontend Page Routes (Subdirectories included)
                .requestMatchers(
                    "/", 
                    "/*.html", 
                    "/admin/**",       // Allows loading /admin/dashboard.html in browser
                  "/error",
                  "/css/**", 
                    "/js/**", 
                    "/images/**", 
                    "/uploads/**", 
                    "/h2-console/**",
                    "/favicon.ico"
                ).permitAll()

                // 2. Authentication & Emergency Unlock Trigger
                .requestMatchers("/api/auth/**", "/api/emergency/unlock").permitAll()

                // 3. Admin Operations (Strict Backend REST Protection)
                .requestMatchers("/api/admin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")

                // 4. Emergency Configuration (User Only)
                .requestMatchers("/api/emergency/recovery-question", "/api/user/settings/**")
                    .hasAnyAuthority("USER", "ROLE_USER")

                // 5. Read-Only Access (GET) for Vault, Memorial, Ghost Engine & Voice Notes
                .requestMatchers(HttpMethod.GET, 
                    "/api/letters/**", 
                    "/vault/letter/**", 
                    "/api/vault/**",
                    "/api/memorial/**", 
                    "/api/ghost-chat/**", 
                    "/api/ghost/**",
                    "/api/voice-notes/**",
                    "/api/photos/**", 
                    "/api/photographs/**"
                ).hasAnyAuthority(
                    "USER", "ROLE_USER", 
                    "FAMILY", "ROLE_FAMILY", 
                    "FAMILY_MEMBER", "ROLE_FAMILY_MEMBER", 
                    "ADMIN", "ROLE_ADMIN"
                )

                // 6. Interactive AI Ghost Chat Access (POST)
                .requestMatchers(HttpMethod.POST, 
                    "/api/ghost-chat/**",
                    "/api/ghost/**"
                ).hasAnyAuthority(
                    "USER", "ROLE_USER", 
                    "FAMILY", "ROLE_FAMILY", 
                    "FAMILY_MEMBER", "ROLE_FAMILY_MEMBER", 
                    "ADMIN", "ROLE_ADMIN"
                )

                // 7. Write/Modify Operations (POST, PUT, DELETE)
                .requestMatchers(HttpMethod.POST, 
                    "/api/letters/**", 
                    "/vault/letter/**", 
                    "/api/vault/**",
                    "/api/voice-notes/**",
                    "/api/photos/**"
                ).hasAnyAuthority("USER", "ROLE_USER")

                .requestMatchers(HttpMethod.PUT, 
                    "/api/letters/**", 
                    "/vault/letter/**", 
                    "/api/vault/**",
                    "/api/voice-notes/**"
                ).hasAnyAuthority("USER", "ROLE_USER")

                .requestMatchers(HttpMethod.DELETE, 
                    "/api/letters/**", 
                    "/vault/letter/**", 
                    "/api/vault/**",
                    "/api/voice-notes/**",
                    "/api/photos/**"
                ).hasAnyAuthority("USER", "ROLE_USER")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
