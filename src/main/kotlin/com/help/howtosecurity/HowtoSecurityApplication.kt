package com.help.howtosecurity

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.reactive.config.EnableWebFlux

@SpringBootApplication
@EnableWebFlux
class HowtoSecurityApplication

fun main(args: Array<String>) {
    runApplication<HowtoSecurityApplication>(*args)
}
