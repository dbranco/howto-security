package com.help.howtosecurity.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono


@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

//    @Bean
//    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
//        return http
//            .httpBasic().authenticationManager { basicAuthenticationDummyAuthentication(it) }.and()
//            .oauth2ResourceServer().jwt().and().and()
//            .authorizeExchange()
//            .pathMatchers("/permitall/**").permitAll()
//            .pathMatchers("/securebasic/**").access{
//                    authMono, _ -> authMono.map { authentication ->
//                canAccess(authentication, "basic")
//            }
//            }
//            .pathMatchers("/secureoauth/**").access{
//                authMono, _ -> authMono.map { authentication ->
//                    canAccess(authentication, "dummy")
//                }
//            }
//            .and().build()
//    }

    @Bean
    @Order(1)
    fun permitAllWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        return http
            .authorizeExchange()
            .pathMatchers("/permitall/**").permitAll()
            .and().build()
    }

    @Bean
    @Order(2)
    fun basicAuthWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        return http
            .httpBasic().authenticationManager { basicAuthenticationDummyAuthentication(it) }.and()
            .authorizeExchange()
            .pathMatchers("/securebasic/**").access {
                authMono, _ -> authMono.map { authentication ->
                    canAccess(authentication, "basic")
                }
            }
            .and().build()
    }

    @Bean
    @Order(3)
    fun oAuthWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        return http
            .oauth2ResourceServer().jwt().and().and()
            .authorizeExchange()
            .pathMatchers("/secureoauth/**").access{
                authMono, _ -> authMono.map { authentication ->
                    canAccess(authentication, "dummy")
                }
            }
            .and().build()
    }

    private fun canAccess(authentication: Authentication, nameToCompare: String): AuthorizationDecision {
        when(authentication) {
            is OAuth2AuthenticationToken -> {
                if ((authentication.principal as DefaultOidcUser).userInfo.givenName == nameToCompare) {
                    return AuthorizationDecision(true)
                }
            }
        }
        if (authentication.name == nameToCompare) {
            return AuthorizationDecision(true)
        }
        return AuthorizationDecision(false)
    }

    private fun basicAuthenticationDummyAuthentication(it: Authentication?) = when (it) {
        is UsernamePasswordAuthenticationToken -> {
            when ((it.name == "basic" || it.name == "dummy") && it.credentials == "auth") {
                true -> Mono.just<Authentication>(UsernamePasswordAuthenticationToken(it.principal, it.credentials, it.authorities))
                else -> Mono.error(BadCredentialsException("Invalid credentials"))
            }
        }
        else -> Mono.empty()
    }

}