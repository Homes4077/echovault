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
                        // Allow CORS preflight OPTIONS requests globally
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 1. Static Assets & Frontend Page Routes
                        .requestMatchers(
                                "/",
                                "/*.html",
                                "/admin/**",
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

                        // 3. Voice Notes & Ghost AI Chat APIs
                        .requestMatchers(
                                "/api/voice-notes",
                                "/api/voice-notes/**",
                                "/api/ghost/**",
                                "/api/ghost-chat/**",
                                "/api/ghost-engine/**",
                                "/ghost/**"
                        ).permitAll()

                        // 4. Admin Operations
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")

                        // 5. User Profile & Emergency Configuration
                        .requestMatchers(
                                "/api/user/**",
                                "/api/user/profile",
                                "/api/emergency/recovery-question",
                                "/api/user/settings/**"
                        ).hasAnyAuthority("USER", "ROLE_USER", "ADMIN", "ROLE_ADMIN")

                        // 6. Read-Only Access (GET) for Vault, Memorial
                        .requestMatchers(HttpMethod.GET,
                                "/api/letters/**",
                                "/vault/letter/**",
                                "/api/vault/**",
                                "/api/memorial/**",
                                "/api/photos/**",
                                "/api/photographs/**"
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
                                "/api/photos/**"
                        ).hasAnyAuthority("USER", "ROLE_USER", "ADMIN", "ROLE_ADMIN")

                        .requestMatchers(HttpMethod.PUT,
                                "/api/letters/**",
                                "/vault/letter/**",
                                "/api/vault/**"
                        ).hasAnyAuthority("USER", "ROLE_USER", "ADMIN", "ROLE_ADMIN")

                        .requestMatchers(HttpMethod.DELETE,
                                "/api/letters/**",
                                "/vault/letter/**",
                                "/api/vault/**",
                                "/api/photos/**"
                        ).hasAnyAuthority("USER", "ROLE_USER", "ADMIN", "ROLE_ADMIN")

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