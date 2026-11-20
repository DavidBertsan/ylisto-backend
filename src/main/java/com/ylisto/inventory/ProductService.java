package com.ylisto.inventory;

import com.ylisto.business.Business;
import com.ylisto.business.BusinessService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service

public class ProductService {
    private final ProductRepository productRepository;
    private final BusinessService businessService;


    public ProductService(ProductRepository productRepository,BusinessService businessService) {
        this.productRepository = productRepository;
        this.businessService= businessService;
    }


    public Product create(UUID businessId, CreateProductRequest request) {
        Business business = businessService.findById(businessId);
        Product product = new Product(business, request.name(), request.imageUrl(), request.unit());
        return productRepository.save(product);   // save + return the saved one (now with its id)
    }



    public List<Product> findAll(UUID businessId) {
        return productRepository.findAllByBusinessId(businessId);
    }

    public Product findById(UUID productId,UUID businessId) {
        return productRepository.findByIdAndBusinessId(productId,businessId).orElseThrow(()->new ProductNotFoundException("Product not Found with UUID: "+productId));
    }


}
