package com.ylisto.signup;

import com.ylisto.business.Business;
import com.ylisto.business.BusinessService;
import com.ylisto.user.Role;
import com.ylisto.user.User;
import com.ylisto.user.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupService {

    private final UserService userService;
    private final BusinessService businessService;

    public SignupService(UserService userService, BusinessService businessService) {
        this.userService = userService;
        this.businessService = businessService;
    }
    @Transactional
    public SignupResponse signup(SignupRequest request){
        Business business=businessService.create(request.businessName(),request.businessType());
        User admin=userService.register(request.username(),request.password(),business,request.name(),request.mobile(), Role.ADMIN,request.imageUrl());
        return SignupResponse.from(business,admin);
    }
}
