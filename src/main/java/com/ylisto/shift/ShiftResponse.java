package com.ylisto.shift;



import java.time.Instant;
import java.util.UUID;

public record ShiftResponse(UUID id, UUID employeeId,  Instant startsAt, Instant endsAt) {
    public static ShiftResponse from(Shift shift){
        return new ShiftResponse(shift.getId(),shift.getUser().getId(),shift.getStartsAt(),shift.getEndsAt());
    }
}
