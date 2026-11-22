package com.ylisto.user;

import java.util.UUID;

public record ProfileResponse(UUID userId, String username, String name, String mobile, String imageUrl, Role role) {
    public static ProfileResponse from(User user){
        return new ProfileResponse(user.getId(),user.getUsername(),user.getName(),user.getMobile(),user.getImageUrl(),user.getRole());
    }
}
