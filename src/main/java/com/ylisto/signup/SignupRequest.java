package com.ylisto.signup;

import com.ylisto.user.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;





public record SignupRequest(
        @NotBlank String businessName,
        String businessType,
        @NotBlank String username,
        @NotBlank @Size(min = 12, max = 72) String password,
        @NotBlank String name,
        @NotBlank String mobile,
        String imageUrl
) { }


