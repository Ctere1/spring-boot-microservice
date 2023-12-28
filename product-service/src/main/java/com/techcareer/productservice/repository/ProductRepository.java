package com.techcareer.productservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techcareer.productservice.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

	Optional<Product> findById(Long id);
}
