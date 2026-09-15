package com.example.taskmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/")
@Tag(name = "Home", description = "Root endpoint")
public class HomeController {

    @GetMapping
    @Operation(summary = "Welcome endpoint")
    public ResponseEntity<Map<String, Object>> home() {
        return ResponseEntity.ok(Map.of(
                "message", "Welcome to Task Manager API",
                "version", "1.0.0",
                "timestamp", LocalDateTime.now(),
                "documentation", "/swagger-ui.html",
                "health", "/api/health",
                "api", "/api/tasks"
        ));
    }
}