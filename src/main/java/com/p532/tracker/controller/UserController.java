package com.p532.tracker.controller;

import com.p532.tracker.domain.User;
import com.p532.tracker.domain.UserRole;
import com.p532.tracker.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * CLIENT LAYER — UserController (Week 2, Change 3)
 *
 * Exposes users so the frontend login dropdown can populate itself.
 * No authentication — just a UI-level user selector.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping
    public List<User> listUsers() {
        return userRepo.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            UserRole role   = UserRole.valueOf(body.getOrDefault("role", "CLINICIAN").toUpperCase());
            if (username == null || username.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "username is required"));
            }
            User user = new User(username, role);
            return ResponseEntity.ok(userRepo.save(user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
