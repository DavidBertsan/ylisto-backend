package com.ylisto.clocking;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}")
public class ClockingController {
    private final ClockingService clockingService;

    public ClockingController(ClockingService clockingService) {
        this.clockingService = clockingService;
    }
    @PostMapping("/clock-in")
    public ResponseEntity<ClockingResponse>  clockIn(@PathVariable UUID userId){

        return ResponseEntity.status(HttpStatus.CREATED).body(ClockingResponse.from(clockingService.clockIn(userId)));
    }
    @PostMapping("/clock-out")
    public ResponseEntity<ClockingResponse>  clockOut(@PathVariable UUID userId){

        return ResponseEntity.ok(ClockingResponse.from(clockingService.clockOut(userId)));
    }

}
