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
            // Disable frame options to allow H2 Console to render inside browser iframes
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 1. Public Frontend Views, Static Assets & H2 Console
                .requestMatchers(
                    "/", 
                    "/*.html", 
                    "/css/**", 
                    "/js/**", 
                    "/images/**", 
                    "/uploads/**", 
                    "/h2-console/**",
                    "/favicon.ico"
                ).permitAll()

                // 2. Public Authentication & Emergency Unlocking
                .requestMatchers("/api/auth/**", "/api/emergency/unlock").permitAll()

                // 3. Photo Endpoints - Temporarily Permitted for Testing
                .requestMatchers("/api/photos/**", "/api/photographs/**").permitAll()

                // 4. Admin-Only Endpoints
                .requestMatchers("/api/admin/**").hasAnyAuthority("ADMIN", "ROLE_ADMIN")

                // 5. Emergency Question Configuration
                .requestMatchers("/api/emergency/recovery-question").hasAnyAuthority("USER", "ROLE_USER")

                // 6. Vault & Letter Operations
                .requestMatchers(HttpMethod.GET, "/api/letters/**", "/vault/letter/**", "/api/voice-notes/**", "/api/ghost-chat/**", "/api/memorial/**")
                    .permitAll()
                
                .requestMatchers(HttpMethod.POST, "/api/letters/**", "/vault/letter/**", "/api/voice-notes/**")
                    .permitAll()
                
                .requestMatchers(HttpMethod.DELETE, "/api/letters/**", "/vault/letter/**", "/api/voice-notes/**")
                    .hasAnyAuthority("USER", "ROLE_USER")

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
