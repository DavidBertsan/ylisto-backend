package com.ylisto.inventory;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


 interface ProductRepository extends JpaRepository<Product, UUID> {

     List<Product> findAllByBusinessId(UUID businessId);
     Optional<Product> findByIdAndBusinessId(UUID productId, UUID businessId);



}
