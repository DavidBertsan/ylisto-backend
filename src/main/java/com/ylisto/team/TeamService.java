package com.ylisto.team;

import com.ylisto.business.Business;
import com.ylisto.business.BusinessService;
import com.ylisto.user.Role;
import com.ylisto.user.User;
import com.ylisto.user.UserNotFoundException;
import com.ylisto.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TeamService {
    private final UserService userService;
    private final BusinessService businessService ;

    public TeamService(UserService userService, BusinessService businessService) {
        this.userService = userService;
        this.businessService = businessService;
    }

    public List<TeamMemberResponse> list(UUID businessId) {
        return userService.findAllByBusinessIdAndActiveTrue(businessId).stream()
                .map(TeamMemberResponse::from)
                .toList();
    }
    public TeamMemberResponse addEmployee(UUID businessId, CreateEmployeeRequest request) {
        Business business = businessService.findById(businessId);
        User employee = userService.register(request.username(), request.password(), business,
                request.name(), request.mobile(), Role.EMPLOYEE, request.imageUrl());
        return TeamMemberResponse.from(employee);
    }
    @Transactional
    public void remove(UUID businessId, UUID adminId, UUID employeeId){
        if(adminId.equals(employeeId)) {
            throw new CannotRemoveYourselfException("Cannot remove yourself");
        }
        User employee = userService.findByIdAndBusinessId(employeeId,businessId).orElseThrow(()-> new UserNotFoundException("User not found: " + employeeId));
        employee.deactivate();
    }



}

