package com.cirt.osint_dashboard.controller;

import com.cirt.osint_dashboard.dto.ChangePasswordRequest;
import com.cirt.osint_dashboard.model.User;
import com.cirt.osint_dashboard.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /* =========================
       LOGIN
       ========================= */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {

        Map<String, Object> response = new HashMap<>();

        if (request.getUsername() == null || request.getPassword() == null) {
            response.put("success", false);
            response.put("message", "Username and password are required");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Invalid username or password");
            return ResponseEntity.ok(response);
        }

        User user = userOpt.get();

        // Plain text comparison (OK for now)
        if (!user.getPassword().equals(request.getPassword())) {
            response.put("success", false);
            response.put("message", "Invalid username or password");
            return ResponseEntity.ok(response);
        }

        if (!user.isActive()) {
            response.put("success", false);
            response.put("message", "Account is deactivated");
            return ResponseEntity.ok(response);
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("fullName", user.getFullName());
        userData.put("role", user.getRole());

        response.put("success", true);
        response.put("message", "Login successful");
        response.put("token", UUID.randomUUID().toString());
        response.put("user", userData);

        return ResponseEntity.ok(response);
    }

    /* =========================
       CHANGE PASSWORD
       ========================= */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody ChangePasswordRequest request) {

        Map<String, Object> response = new HashMap<>();

        if (request.getUsername() == null ||
                request.getOldPassword() == null ||
                request.getNewPassword() == null) {

            response.put("success", false);
            response.put("message", "All fields are required");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.ok(response);
        }

        User user = userOpt.get();

        if (!user.getPassword().equals(request.getOldPassword())) {
            response.put("success", false);
            response.put("message", "Current password is incorrect");
            return ResponseEntity.ok(response);
        }

        user.setPassword(request.getNewPassword());
        userRepository.save(user);

        response.put("success", true);
        response.put("message", "Password changed successfully");

        return ResponseEntity.ok(response);
    }

    /* =========================
       LOGOUT
       ========================= */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Logged out successfully"
        ));
    }

    /* =========================
       LOGIN DTO (INNER)
       ========================= */
    static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
