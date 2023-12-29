package com.techcareer.shoppingcartservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techcareer.shoppingcartservice.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	Optional<Product> findById(Long id);
}
