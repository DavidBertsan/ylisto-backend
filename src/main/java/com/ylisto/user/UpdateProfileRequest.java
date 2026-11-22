package com.ylisto.user;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(@NotBlank String name, @NotBlank String mobile, String imageUrl) {
}
