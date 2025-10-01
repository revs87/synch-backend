package com.rvcoding.synch.security

import com.rvcoding.synch.api.security.JwtAuthFilter
import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig {

    @Bean
    fun filterChain(
        httpSecurity: HttpSecurity,
        jwtAuthFilter: JwtAuthFilter
    ): SecurityFilterChain {
        return httpSecurity
            .csrf { it.disable() } // Cross-site request forgery
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/auth/change-password").authenticated()
                    .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll() // Spring internal error handler. Respond with permitAll (unauthorize bypass).
                    .anyRequest().authenticated() // Refers to all requests that were not explicitly define above
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling { configurer ->
                configurer
                    .authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // If something above fails, returns 401.
            }
            .build()
    }
}