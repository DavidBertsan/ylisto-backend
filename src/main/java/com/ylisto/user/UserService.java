package com.ylisto.user;

import com.ylisto.business.Business;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String username, String rawPassword, Business business,String name, String mobile, Role role, String imageUrl){
        if(userRepository.existsByUsername(username)){
            throw new UsernameAlreadyExistsException("Username already exists: " + username);
        }
        String hashedPassword = passwordEncoder.encode(rawPassword);
        User user=new User(username,hashedPassword,business,name,mobile,role,imageUrl);

        return userRepository.save(user);
    }

}
