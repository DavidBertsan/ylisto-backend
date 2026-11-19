package com.ylisto.clocking;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/clockings")
public class ClockingController {
    private final ClockingService clockingService;

    public ClockingController(ClockingService clockingService) {
        this.clockingService = clockingService;
    }
    @PostMapping("/clock-in")
    public ResponseEntity<ClockingResponse>  clockIn(@AuthenticationPrincipal Jwt jwt){
          UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(ClockingResponse.from(clockingService.clockIn(userId)));
    }
    @PostMapping("/clock-out")
    public ResponseEntity<ClockingResponse>  clockOut(@AuthenticationPrincipal Jwt jwt){
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(ClockingResponse.from(clockingService.clockOut(userId)));
    }

}
