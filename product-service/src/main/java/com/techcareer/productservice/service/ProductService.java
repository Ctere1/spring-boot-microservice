package com.techcareer.productservice.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.techcareer.productservice.entity.Product;
import com.techcareer.productservice.repository.ProductRepository;

@Service
public class ProductService {

	@Autowired
	ProductRepository productRepository;

	public ResponseEntity<Product> getProductById(Long productId) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Product not found in DB"));

		return ResponseEntity.ok(product);
	}

	public ResponseEntity<List<Product>> allProducts() {

		return ResponseEntity.ok(productRepository.findAll());
	}

	public ResponseEntity<Product> createProduct(Product product) {

		return ResponseEntity.ok().body(productRepository.save(product));
	}

	public ResponseEntity<Product> updateProduct(Long productId, Product updatedProduct) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new RuntimeException("Product not found in DB"));

		product.setName(updatedProduct.getName());
		product.setPrice(updatedProduct.getPrice());
		product.setCategory(updatedProduct.getCategory());
		product.setDescription(updatedProduct.getDescription());

		productRepository.save(product);
		return ResponseEntity.ok(product);

	}

	public ResponseEntity<String> deleteProduct(Long id) {
		if (productRepository.existsById(id)) {
			productRepository.deleteById(id);
			return ResponseEntity.ok("Product deleted successfully");
		} else {
			throw new RuntimeException("Product not found in DB");
		}
	}

	public ResponseEntity<String> deleteAllProducts() {
		productRepository.deleteAll();
		return ResponseEntity.ok("All products deleted successfully");
	}

}
