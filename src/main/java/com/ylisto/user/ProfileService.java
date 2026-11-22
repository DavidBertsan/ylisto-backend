package com.ylisto.user;


import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProfileService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    public ProfileService(UserService userService,PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder =passwordEncoder;
    }

    public User getProfile(UUID userId){
        return userService.findById(userId);
    }
    @Transactional
    public User updateProfile(UUID userId, String name, String mobile, String imageUrl){
        User user=userService.findById(userId);
        user.updateProfile(name,mobile,imageUrl);
        return user;
    }
    @Transactional
    public User changePassword(UUID userId, String currentPassword, String newPassword){
       User user=userService.findById(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }
       user.changePassword(passwordEncoder.encode(newPassword));
        return user;
    }
}
