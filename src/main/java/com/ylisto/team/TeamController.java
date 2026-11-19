package com.ylisto.team;

import com.ylisto.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/team")
public class TeamController {
    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ResponseEntity<List<TeamMemberResponse>> list(@AuthenticationPrincipal Jwt jwt) {

        UUID businessId = UUID.fromString(jwt.getClaimAsString("businessId"));
        return ResponseEntity.ok(teamService.list(businessId));
    }

    @PostMapping
    public ResponseEntity<TeamMemberResponse> addEmployee(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateEmployeeRequest request) {
        UUID businessId = UUID.fromString(jwt.getClaimAsString("businessId"));
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.addEmployee(businessId,request));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> remove(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID userId) {
        UUID businessId = UUID.fromString(jwt.getClaimAsString("businessId"));
        UUID adminId = UUID.fromString(jwt.getSubject());
        teamService.remove(businessId, adminId, userId);
        return ResponseEntity.noContent().build();
    }
}