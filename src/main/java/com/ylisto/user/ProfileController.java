package com.ylisto.user;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(ProfileResponse.from(profileService.getProfile(userId)));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());

        return ResponseEntity.ok(ProfileResponse.from(
                profileService.updateProfile(userId, request.name(), request.mobile(), request.imageUrl())));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePasswordRequest request) {
        UUID userId = UUID.fromString(jwt.getSubject());

        profileService.changePassword(userId, request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}
