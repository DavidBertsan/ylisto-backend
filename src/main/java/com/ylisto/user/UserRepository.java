package com.ylisto.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

 interface UserRepository extends JpaRepository<User, UUID> {
     boolean existsByUsername(String username);
     Optional<User> findByUsername(String username);
     Optional<User> findByIdAndBusinessId(UUID id, UUID businessId);
     List<User> findAllByBusinessIdAndActiveTrue(UUID businessId);
}
