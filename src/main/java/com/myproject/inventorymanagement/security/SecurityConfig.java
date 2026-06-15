package com.myproject.inventorymanagement.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // set cors mac dinh *, change later?
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/", "/index.html", "/login.html", "/static/**", "/*.html", "/*.js", "/*.css",
                                "/favicon.ico")
                        .permitAll()

                        // 1. User Management (ADMIN)
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // 2. Products, Categories, Suppliers (MANAGER, STAFF)
                        .requestMatchers("/api/products/**").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers("/api/categories/**").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers("/api/suppliers/**").hasAnyRole("MANAGER", "STAFF")

                        // 3. Stock Requests
                        // - Approval/Rejection (MANAGER)
                        .requestMatchers(HttpMethod.POST, "/api/stock-requests/*/approve",
                                "/api/stock-requests/*/reject")
                        .hasRole("MANAGER")
                        // - Creation (STAFF)
                        .requestMatchers(HttpMethod.POST, "/api/stock-requests").hasRole("STAFF")
                        // - Viewing/Details (MANAGER, STAFF)
                        .requestMatchers("/api/stock-requests/**").hasAnyRole("MANAGER", "STAFF")

                        // 4. Invoices (MANAGER, STAFF)
                        .requestMatchers("/api/invoices/**").hasAnyRole("MANAGER", "STAFF")

                        // 5. Stock Movements (MANAGER)
                        .requestMatchers("/api/stock-movements/**").hasRole("MANAGER")

                        // 6. Statistics/Dashboard (MANAGER)
                        .requestMatchers("/api/statistics/**").hasRole("MANAGER")

                        .anyRequest().authenticated());

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
