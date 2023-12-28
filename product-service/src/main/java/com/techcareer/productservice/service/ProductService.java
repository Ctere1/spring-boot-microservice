package com.techcareer.productservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import com.techcareer.productservice.entity.Product;
import com.techcareer.productservice.repository.ProductRepository;

@Service
public class ProductService {

	@Autowired
	ProductRepository productRepository;

	public ResponseEntity<Product> queryById(Long id) {

		Product product = productRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Product not found in DB"));

		return ResponseEntity.ok(product);
	}

	public ResponseEntity<List<Product>> allProducts() {

		return ResponseEntity.ok(productRepository.findAll());
	}

	public ResponseEntity<Product> createProduct(Product product) {

		return ResponseEntity.ok().body(productRepository.save(product));
	}
}
