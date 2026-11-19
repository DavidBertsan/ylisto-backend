package com.ylisto.team;

import com.ylisto.user.Role;
import com.ylisto.user.User;

import java.util.UUID;

public record TeamMemberResponse(UUID id, String username, String name, String mobile, Role role) {

    public static  TeamMemberResponse from(User user){
        return new TeamMemberResponse(user.getId(),user.getUsername(),user.getName(),user.getMobile(),user.getRole());
    }
}
