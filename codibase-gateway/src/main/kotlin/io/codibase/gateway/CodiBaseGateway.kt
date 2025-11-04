package io.codibase.gateway

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import kotlinx.coroutines.*

@SpringBootApplication
class CodiBaseGateway

fun main(args: Array<String>) {
    runApplication<CodiBaseGateway>(*args)
}

@RestController
@RequestMapping("/api/v1")
class UnifiedApiController {
    
    private val restTemplate = RestTemplate()
    
    @GetMapping("/health")
    fun health(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(mapOf(
            "status" to "healthy",
            "services" to mapOf(
                "codibase" to checkService("http://codibase:6610/health"),
                "pilotcodi" to checkService("http://pilotcodi:8081/health"),
                "telemetry" to checkService("http://telemetry:8080/health")
            )
        ))
    }
    
    @PostMapping("/ai/complete")
    fun aiComplete(@RequestBody request: Map<String, Any>): ResponseEntity<Map<String, Any>> {
        // Route to PilotCodi for fast completions or Claude API for complex requests
        val complexity = request["complexity"] as? String ?: "simple"
        
        return if (complexity == "simple") {
            // Use PilotCodi (in-house Tabby)
            val response = restTemplate.postForObject(
                "http://pilotcodi:8081/v1/completions",
                request,
                Map::class.java
            )
            ResponseEntity.ok(response as Map<String, Any>)
        } else {
            // Use Claude API for complex tasks
            val response = restTemplate.postForObject(
                "http://pilotcodi:8081/v1/chat",
                request,
                Map::class.java
            )
            ResponseEntity.ok(response as Map<String, Any>)
        }
    }
    
    @PostMapping("/telemetry/track")
    fun trackEvent(@RequestBody event: Map<String, Any>): ResponseEntity<Map<String, Any>> {
        restTemplate.postForObject(
            "http://telemetry:8080/collect",
            event,
            Map::class.java
        )
        return ResponseEntity.ok(mapOf("status" to "tracked"))
    }
    
    private fun checkService(url: String): String {
        return try {
            restTemplate.getForObject(url, String::class.java)
            "healthy"
        } catch (e: Exception) {
            "unhealthy"
        }
    }
}

@Bean
fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
    return builder.routes()
        .route("codibase") { r ->
            r.path("/git/**", "/projects/**", "/builds/**")
                .uri("http://codibase:6610")
        }
        .route("pilotcodi") { r ->
            r.path("/ai/**")
                .uri("http://pilotcodi:8081")
        }
        .route("telemetry") { r ->
            r.path("/analytics/**")
                .uri("http://telemetry:8080")
        }
        .build()
}
