package com.techcareer.shoppingcartservice.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.techcareer.shoppingcartservice.entity.Product;
import com.techcareer.shoppingcartservice.entity.ShoppingCart;
import com.techcareer.shoppingcartservice.repository.ProductRepository;
import com.techcareer.shoppingcartservice.repository.ShoppingCartRepository;

@Service
public class ShoppingCartService {

	@Autowired
	ShoppingCartRepository shoppingCartRepository;

	@Autowired
	ProductRepository productRepository;

	@Autowired
	RestTemplate restTemplate;

	public ResponseEntity<ShoppingCart> createCart(String name) {
		ShoppingCart shoppingCart = new ShoppingCart();
		shoppingCart.setShoppingCartName(name);
		return ResponseEntity.ok().body(shoppingCartRepository.save(shoppingCart));
	}

	public ResponseEntity<ShoppingCart> addProducts(Long shoppingCartId, List<Product> products) {

		ShoppingCart shoppingCart = shoppingCartRepository.findById(shoppingCartId)
				.orElseThrow(() -> new RuntimeException("Shopping cart not found"));

		products.forEach(product -> productRepository.saveAndFlush(product));
		Set<Product> newProducts = new HashSet<>(products);

		shoppingCart.setProducts(newProducts);

		return ResponseEntity.ok().body(shoppingCartRepository.save(shoppingCart));
	}

	public ResponseEntity<Map<String, String>> getShoppingCartPrice(Long shoppingCartId) {
		Map<String, String> response = new HashMap<>();

		ShoppingCart shoppingCart = shoppingCartRepository.findById(shoppingCartId)
				.orElseThrow(() -> new RuntimeException("Shopping cart not found"));

		int totalPrice = shoppingCart
				.getProducts().stream().map(product -> restTemplate
						.getForObject("http://PRODUCT-SERVICE/api/product/" + product.getId(), HashMap.class))
				.mapToInt(productResponse -> (int) productResponse.get("price")).sum();
		response.put("Total Price", Double.toString(totalPrice));

		return ResponseEntity.ok().body(response);
	}
}
