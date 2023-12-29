package com.techcareer.productservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.techcareer.productservice.entity.Product;
import com.techcareer.productservice.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("api/product")
@Tag(name = "product", description = "Product Endpoints")
public class ProductController {

	@Autowired
	ProductService productService;

	@GetMapping
	@Operation(summary = "Get all Products", description = "Get all Products")
	public ResponseEntity<List<Product>> getAll() {
		return productService.allProducts();
	}

	@GetMapping("{id}")
	public ResponseEntity<Product> getProductById(@PathVariable("id") Long productId) {
		return productService.getProductById(productId);
	}

	@PostMapping
	public ResponseEntity<Product> createProduct(@RequestBody Product product) {
		return productService.createProduct(product);
	}

	@PutMapping("{id}")
	public ResponseEntity<Product> updateProduct(@PathVariable("id") Long productId,
			@RequestBody Product updatedProduct) {
		return productService.updateProduct(productId, updatedProduct);
	}

	@DeleteMapping("{id}")
	public ResponseEntity<String> deleteById(@PathVariable("id") Long id) {
		return productService.deleteProduct(id);
	}

	@DeleteMapping("/deleteAll")
	public ResponseEntity<String> deleteAll() {
		return productService.deleteAllProducts();
	}

}
