package com.ylisto.shift;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequestMapping("/shifts")
@RestController
public class ShiftController {
    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @PostMapping
    public ResponseEntity<ShiftResponse> create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AssignShiftRequest request){
        UUID businessId = UUID.fromString(jwt.getClaimAsString("businessId"));

        return ResponseEntity.status(HttpStatus.CREATED).body(ShiftResponse.from(shiftService.assign(businessId,request.employeeId(),request.startsAt(),request.endsAt())));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ShiftResponse>> mySchedule(@AuthenticationPrincipal Jwt jwt, @RequestParam Instant from, @RequestParam Instant to){
        UUID userId = UUID.fromString(jwt.getSubject());

        return  ResponseEntity.ok(shiftService.mySchedule(userId, from, to).stream().map(ShiftResponse::from).toList());
    }
    @DeleteMapping("/{shiftId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt,@PathVariable UUID shiftId){
        UUID businessId = UUID.fromString(jwt.getClaimAsString("businessId"));
        shiftService.remove(businessId, shiftId);
        return ResponseEntity.noContent().build();
    }
}
