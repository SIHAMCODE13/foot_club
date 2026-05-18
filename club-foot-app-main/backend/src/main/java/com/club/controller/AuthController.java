package com.club.controller;

import com.club.model.User;
import com.club.security.JwtUtil;
import com.club.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
// @CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User createdUser = userService.register(user);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Inscription réussie");
            response.put("user", createdUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/check-status")
    public ResponseEntity<?> checkActivationStatus(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            if (email == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email requis"));
            }

            Map<String, Object> status = userService.checkActivationStatus(email);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // NEW: Activate account (first-time login)
    @PostMapping("/activate")
    public ResponseEntity<?> activateAccount(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String activationToken = request.get("activationToken");

            if (email == null || password == null || activationToken == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Email, mot de passe et token d'activation requis"
                ));
            }

            // Activate the account
            User user = userService.activateAccount(email, password, activationToken);

            // Generate JWT token
            String token = jwtUtil.generateToken(user);

            logger.info("Compte activé avec succès: {}", email);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Compte activé avec succès");
            response.put("token", token);
            response.put("user", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "nom", user.getNom(),
                "prenom", user.getPrenom(),
                "role", user.getRole().toString(),
                "equipeId", user.getEquipeId()
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de l'activation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            String email = credentials.get("email");
            String password = credentials.get("password");

            if (email == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email requis"));
            }

            // First, check activation status
            User user = (User) userService.loadUserByUsername(email);

            if (user.getAccountStatus() == User.AccountStatus.ACTIVATION_REQUISE) {
                // Account not yet activated → redirect to activation
                return ResponseEntity.status(403).body(Map.of(
                    "error", "Compte non activé",
                    "needsActivation", true,
                    "activationToken", user.getActivationToken(),
                    "message", "Veuillez définir votre mot de passe pour activer votre compte"
                ));
            }

            if (user.getAccountStatus() == User.AccountStatus.SUSPENDU) {
                return ResponseEntity.status(403).body(Map.of(
                    "error", "Compte suspendu",
                    "message", "Votre compte a été suspendu. Contactez l'administrateur."
                ));
            }

            // If password is null, it's a first login
            if (password == null || password.isEmpty()) {
                if (user.getPassword() == null) {
                    return ResponseEntity.status(403).body(Map.of(
                        "error", "Mot de passe requis",
                        "needsActivation", true,
                        "activationToken", user.getActivationToken(),
                        "message", "Veuillez définir votre mot de passe"
                    ));
                }
                return ResponseEntity.badRequest().body(Map.of("error", "Mot de passe requis"));
            }

            // Normal authentication
            logger.info("Tentative de connexion pour: {}", email);

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            userService.updateLastLogin(email);
            userService.migratePasswordIfNeeded(email, password);

            User authenticatedUser = (User) userDetails;

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", Map.of(
                    "id", authenticatedUser.getId(),
                    "email", authenticatedUser.getEmail(),
                    "nom", authenticatedUser.getNom(),
                    "prenom", authenticatedUser.getPrenom(),
                    "role", authenticatedUser.getRole().toString(),
                    "equipeId", authenticatedUser.getEquipeId()));

            logger.info("Connexion réussie pour: {}", email);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            logger.warn("Identifiants incorrects: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Email ou mot de passe incorrect"));
        } catch (Exception e) {
            logger.error("Erreur lors de la connexion", e);
            return ResponseEntity.status(500).body(Map.of("error", "Erreur interne du serveur"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
    }
}