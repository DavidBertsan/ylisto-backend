package com.ylisto.team;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEmployeeRequest(@NotBlank String username,
                                    @NotBlank @Size(min = 12, max = 72) String password,
                                    @NotBlank String name,
                                    @NotBlank String mobile,
                                    String imageUrl) {
}
