package com.club.service;

import com.club.dto.CreateUserRequest;
import com.club.dto.DocumentPresentationStatus;
import com.club.dto.DocumentResponse;
import com.club.dto.UserWithDocumentsResponse;
import com.club.exception.ResourceNotFoundException;
import com.club.model.RegistrationStatus;
import com.club.model.User;
import com.club.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final DocumentService documentService;

    public AdminUserService(UserRepository userRepository,
                            DocumentService documentService) {
        this.userRepository = userRepository;
        this.documentService = documentService;
    }

    @Transactional
    public User createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email déjà utilisé");
        }

        // Generate unique activation token
        String activationToken = UUID.randomUUID().toString();

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPrenom(request.getFirstName());
        user.setNom(request.getLastName());
        user.setTelephone(request.getPhone());
        user.setDateNaissance(request.getDateOfBirth() != null ? request.getDateOfBirth().toString() : null);
        user.setRole(request.getRole());
        user.setAdresse(request.getAddress());
        user.setPassword(null);  // No password - user will set it during activation
        user.setActif(false);
        user.setAccountStatus(User.AccountStatus.ACTIVATION_REQUISE);
        user.setActivationToken(activationToken);  // Set activation token
        user.setRegistrationStatus(RegistrationStatus.PENDING);
        user.setDateInscription(java.time.LocalDateTime.now());

        // Set equipeId and poste for JOUEUR role
        if (request.getRole() == User.Role.JOUEUR) {
            user.setEquipeId(request.getEquipeId());
            user.setPoste(request.getPoste());
        }

        User savedUser = userRepository.save(user);
        
        // TODO: Send activation email with link
        // emailService.sendActivationEmail(user.getEmail(), activationToken);
        
        return savedUser;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    public UserWithDocumentsResponse getUserWithDocuments(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<DocumentResponse> checklist = documentService.getDocumentChecklistForUser(userId);

        var completion = documentService.getCompletionStatus(userId);
        int documentsRequired = (int) completion.get("totalRequired");
        int documentsCompleted = (int) completion.get("completed");
        int completionPercentage = documentsRequired > 0
                ? (documentsCompleted * 100) / documentsRequired
                : 0;

        List<DocumentResponse> missingDocuments = checklist.stream()
                .filter(d -> d.getStatus() == DocumentPresentationStatus.MISSING)
                .collect(Collectors.toList());

        return UserWithDocumentsResponse.fromEntity(
                user,
                checklist,
                completionPercentage,
                documentsCompleted,
                documentsRequired,
                missingDocuments
        );
    }

    /**
     * Met à jour le statut d'inscription. ACTIVE n'est accepté que si le dossier documentaire est complet et approuvé.
     */
    @Transactional
    public User updateUserStatus(Long userId, RegistrationStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        if (status == RegistrationStatus.ACTIVE) {
            if (!documentService.areAllMandatoryDocumentsApproved(userId)) {
                throw new IllegalArgumentException(
                        "Impossible d'activer : tous les documents obligatoires doivent être approuvés.");
            }
            user.setActif(true);
            user.setAccountStatus(User.AccountStatus.ACTIF);
        }

        if (status == RegistrationStatus.REJECTED) {
            user.setActif(false);
        }

        user.setRegistrationStatus(status);
        return userRepository.save(user);
    }
}
