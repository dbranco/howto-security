package com.help.howtosecurity.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
import org.springframework.web.reactive.config.EnableWebFlux
import reactor.core.publisher.Mono


@Configuration
@EnableWebFluxSecurity
@EnableWebFlux
class SecurityConfig {
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    var issuerUri: String? = null

    @Bean
    @Order(1)
    fun permitAllWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .httpBasic().disable()
            .securityMatcher(pathMatchers("/permitall/**"))
            .authorizeExchange()
            .pathMatchers("/permitall/**").permitAll()
            .and().build()
    }

    @Bean
    @Order(2)
    fun basicAuthWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain? {
        return http
            .securityMatcher(pathMatchers("/securebasic/**"))
            .httpBasic().authenticationManager { basicAuthenticationDummyAuthentication(it) }.and()
            .authorizeExchange { spec ->
                run {
                    spec.pathMatchers("/securebasic/**").authenticated()
                }
            }
            .exceptionHandling()
            .authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
            .and()
            .build()
    }

    @Bean
    @Order(3)
    fun oAuthWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .httpBasic().disable()
            .securityMatcher(pathMatchers("/secureoauth/**"))
            .authorizeExchange { spec ->
                run {
                    spec.pathMatchers("/secureoauth/**").authenticated()
                }
            }
            .oauth2ResourceServer(
                ServerHttpSecurity.OAuth2ResourceServerSpec::jwt
            )
            .exceptionHandling()
            .authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
            .and().build()
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

    @Bean
    fun reactiveJwtDecoder(): ReactiveJwtDecoder? {
        return ReactiveJwtDecoders.fromIssuerLocation(issuerUri)
    }

}
