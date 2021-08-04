package com.help.howtosecurity.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint
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
//            .httpBasic().disable()
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
// If you only send back the 401 Status you have to provide the Authorization header, but browser won't aware you about
// that, but if you add the WWW-Authenticate=Basic... it will pops thr browser form asking for credentials
//            .authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
            .authenticationEntryPoint { exchange, ex ->
                Mono.fromRunnable {
                    exchange.response.headers.set("WWW-Authenticate", "Basic realm=dummy")
                    exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                }
            }
            .and()
            .build()
    }

    @Bean
    @Order(3)
    fun oAuthWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
//            .httpBasic().disable()
// These 2 "/login/**", "/oauth2/**" patterns were needed to let you been login using Okta
//            .securityMatcher(pathMatchers("/login/**", "/oauth2/**","/secureoauth/**"))
            .authorizeExchange().anyExchange().authenticated().and()
            .oauth2ResourceServer(
                ServerHttpSecurity.OAuth2ResourceServerSpec::jwt
            )
// This is only needed for a broader case.
// For this case using Okta if you don't handle the exception, the Okta libraries are prepared to handle it for you
// redirecting to their login page
//            .exceptionHandling()
//            .authenticationEntryPoint(HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
//            .and()
            .build()
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
