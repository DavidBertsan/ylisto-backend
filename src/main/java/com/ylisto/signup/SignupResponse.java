package com.ylisto.signup;

import com.ylisto.business.Business;
import com.ylisto.user.Role;
import com.ylisto.user.User;

import java.util.UUID;



public record SignupResponse(UUID businessId, String businessName,
                             UUID userId, String username, Role role) {

    public static SignupResponse from(Business business, User user) {
        return new SignupResponse(
                business.getId(),
                business.getName(),
                user.getId(),
                user.getUsername(),
                user.getRole());
    }
}
