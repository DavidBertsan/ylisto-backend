package com.ylisto.shift;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record AssignShiftRequest(@NotNull UUID employeeId,
                                 @NotNull Instant startsAt,
                                 @NotNull Instant endsAt) { }
