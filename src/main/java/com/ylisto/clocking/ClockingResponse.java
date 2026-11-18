package com.ylisto.clocking;

import java.time.Instant;
import java.util.UUID;

public record ClockingResponse(UUID id, Instant clockIn, Instant clockOut) {

    public static ClockingResponse from(Clocking clocking) {
       return new ClockingResponse(clocking.getId(),clocking.getClockIn(),clocking.getClockOut());
    }
}
