package com.techcareer.productservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

	@PostMapping
	public ResponseEntity<Product> save(@RequestBody Product product) {
		return productService.createProduct(product);
	}

	@GetMapping("{id}")
	public ResponseEntity<Product> getById(@PathVariable("id") Long id) {
		return productService.queryById(id);
	}

}
