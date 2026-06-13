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
                        // 1. User Management (ADMIN only)
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        
                        // 2. Products, Categories, Suppliers (MANAGER and STAFF can CRUD)
                        .requestMatchers("/api/products/**").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers("/api/categories/**").hasAnyRole("MANAGER", "STAFF")
                        .requestMatchers("/api/suppliers/**").hasAnyRole("MANAGER", "STAFF")
                        
                        // 3. Stock Requests
                        // - Approval/Rejection (MANAGER only)
                        .requestMatchers(HttpMethod.POST, "/api/stock-requests/*/approve", "/api/stock-requests/*/reject")
                        .hasRole("MANAGER")
                        // - Creation (STAFF only)
                        .requestMatchers(HttpMethod.POST, "/api/stock-requests").hasRole("STAFF")
                        // - Viewing/Details/Exporting (MANAGER and STAFF)
                        .requestMatchers("/api/stock-requests/**").hasAnyRole("MANAGER", "STAFF")
                        
                        // 4. Invoices (MANAGER and STAFF)
                        .requestMatchers("/api/invoices/**").hasAnyRole("MANAGER", "STAFF")
                        
                        // 4. Stock Movements (MANAGER only)
                        .requestMatchers("/api/stock-movements/**").hasRole("MANAGER")
                        
                        // 5. Statistics/Dashboard (MANAGER only)
                        .requestMatchers("/api/statistics/**").hasRole("MANAGER")
                        
                        .anyRequest().authenticated());

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
