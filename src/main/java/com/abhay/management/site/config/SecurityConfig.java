package com.abhay.management.site.config;

import com.abhay.management.site.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // enables @PreAuthorize on controllers
@RequiredArgsConstructor
public class SecurityConfig {

	
	// Inside SecurityConfig class, add this bean:
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
	    CorsConfiguration config = new CorsConfiguration();
	    config.setAllowedOriginPatterns(List.of("*"));
	    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
	    config.setAllowedHeaders(List.of("*"));
	    config.setAllowCredentials(true);
	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    source.registerCorsConfiguration("/**", config);
	    return source;
	}

	
    private final JwtAuthFilter jwtAuthFilter;
    


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        
        // In filterChain(), add .cors() BEFORE .csrf():
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        
            // Disable CSRF — we use stateless JWT, not cookies
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless session — no HttpSession created
            .sessionManagement(sm ->
                    sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Public vs protected routes
            .authorizeHttpRequests(auth -> auth
                    // Auth endpoints are public
                    .requestMatchers("/api/auth/**").permitAll()
                    // Admin-only endpoints
                    //.requestMatchers("/api/admin/**")
                    //.hasRole("ADMIN")
                    .requestMatchers("/api/reports/**").hasRole("ADMIN")
                    // All other endpoints require any valid authenticated user
                    .anyRequest().authenticated()
            )

            // Plug in our JWT filter before Spring's username/password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt with strength 12 — good balance of security and performance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
