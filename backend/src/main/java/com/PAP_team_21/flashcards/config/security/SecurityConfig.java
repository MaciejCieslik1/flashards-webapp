package com.PAP_team_21.flashcards.config.security;

import com.PAP_team_21.flashcards.filters.JtwFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig{

    private final UserDetailsService userDetailsService;

    @Value("${jwt.secret-key}")
    private String jwtSecret;

    @Bean
    protected SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(sessionConfig -> sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable())
                .cors(corsConfig -> corsConfig.configurationSource(new CorsConfigurationSource() {
                    @Override
                    public CorsConfiguration getCorsConfiguration(@NonNull  HttpServletRequest request) {
                        CorsConfiguration config = new CorsConfiguration();
                        config.setAllowedOriginPatterns(Collections.singletonList("*"));
                        config.setAllowedMethods(Collections.singletonList("*"));
                        config.setAllowCredentials(true);
                        config.setAllowedHeaders(Collections.singletonList("*"));
                        config.setMaxAge(3600L);
                        return config;
                    }
                }))
                .authorizeHttpRequests( auth->
                        auth.requestMatchers("/actuator/health")// Allow unauthenticated access to /actuator/health
                        .permitAll()
                        .requestMatchers("/api/auth/register") // allow unauthenticated access to auth endpoints
                        .permitAll()
                        .requestMatchers("/api/auth/usernamePasswordLogin")
                                .permitAll()
                                .requestMatchers("/api/auth/forgotPasswordRequest")
                                .permitAll()
                                .requestMatchers("/api/auth/forgotPassword")
                                .permitAll()
                                .requestMatchers("/api/auth/verifyUser")
                                .permitAll()
                                .requestMatchers("/api/auth/resendVerificationCode")
                                .permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login( oauth -> oauth
                        .defaultSuccessUrl("/api/auth/oauth2/success", true)
                )
                .httpBasic(Customizer.withDefaults())
                .addFilterAfter(new JtwFilter(jwtSecret), BasicAuthenticationFilter.class);


        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public UserDetailsService userDetailsService()
    {
        return userDetailsService;
    }
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder)
    {
        UsernamePasswordAuthentication authProvider = new UsernamePasswordAuthentication(userDetailsService, passwordEncoder);
        ProviderManager providerManager = new ProviderManager(authProvider);
        providerManager.setEraseCredentialsAfterAuthentication(false);
        return providerManager;
    }
}