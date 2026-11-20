package com.ylisto.inventory;

import com.ylisto.business.Business;
import jakarta.persistence.*;
import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;


@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="id",updatable = false,nullable = false)
    private UUID id;
    @ManyToOne(optional = false, fetch = LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;
    @Column(nullable = false)
    private String name;
    private String imageUrl;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit;

    public  Product(Business business ,String name, String imageUrl, Unit unit){
        this.business=business;
        this.name=name;
        this.imageUrl=imageUrl;
        this.unit=unit;
    }
    protected   Product(){

    }



    public UUID getId() {
        return id;
    }

    public  Business getBusiness(){ return business;}
    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }


    public Unit getUnit() {
        return unit;
    }


}
