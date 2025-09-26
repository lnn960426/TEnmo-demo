package com.techelevator.tenmo.security;

import com.techelevator.tenmo.security.jwt.JWTCustomDSL;
import com.techelevator.tenmo.security.jwt.TokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final UserModelDetailsService userModelDetailsService;

    public WebSecurityConfig(
            TokenProvider tokenProvider,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAccessDeniedHandler jwtAccessDeniedHandler,
            UserModelDetailsService userModelDetailsService
    ) {
        this.tokenProvider = tokenProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.userModelDetailsService = userModelDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        // Keep ignoring CORS preflight requests
        return (web) -> web.ignoring().requestMatchers(HttpMethod.OPTIONS, "/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                // --- allow unauthenticated access for demo endpoints and static demo page ---
                .authorizeHttpRequests(auth -> auth
                        // Public demo endpoints
                        .requestMatchers("/demo/**", "/demo.html").permitAll()
                        // (optional) health check & root/static assets if needed
                        .requestMatchers("/", "/health", "/index.html", "/css/**", "/js/**", "/images/**").permitAll()
                        // Login endpoint can stay public if your API expects it
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // --- standard exception & stateless session config ---
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // --- apply JWT filter chain ---
                .apply(securityConfigurerAdapter());

        return http.build();
    }

    private JWTCustomDSL securityConfigurerAdapter() {
        return new JWTCustomDSL(tokenProvider);
    }
}