package com.ddiring.backend_market.investment.repository;

import com.ddiring.backend_market.investment.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    

    @Query("SELECT p FROM Product p WHERE p.status = 1 ORDER BY p.createdAt DESC")
    List<Product> findApprovedProducts();


    List<Product> findByStatus(Integer status);
    

    List<Product> findByUserSeq(Integer userSeq);
} 