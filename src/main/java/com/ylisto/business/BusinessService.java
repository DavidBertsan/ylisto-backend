package com.ylisto.business;


import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BusinessService {
    private final BusinessRepository businessRepository;

    public BusinessService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    public Business create(String name,String type){
        return businessRepository.save(new Business(name,type));
    }

    public Business findById(UUID id){
       return  businessRepository.findById(id)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found: " + id));
    }

}
