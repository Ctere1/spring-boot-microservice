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

		Set<Product> existingProducts = shoppingCart.getProducts();
		if (existingProducts == null) {
			existingProducts = new HashSet<>();
		}
		existingProducts.addAll(newProducts);

		shoppingCart.setProducts(existingProducts);

		return ResponseEntity.ok().body(shoppingCartRepository.save(shoppingCart));
	}

	public ResponseEntity<ShoppingCart> removeProduct(Long shoppingCartId, Long productId) {
		ShoppingCart shoppingCart = shoppingCartRepository.findById(shoppingCartId)
				.orElseThrow(() -> new RuntimeException("Shopping cart not found"));

		Set<Product> existingProducts = shoppingCart.getProducts();
		if (existingProducts == null) {
			return ResponseEntity.ok().body(shoppingCart);
		}

		existingProducts.removeIf(product -> product.getId() == (productId));

		shoppingCart.setProducts(existingProducts);

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
		response.put("total_price", Double.toString(totalPrice));

		return ResponseEntity.ok().body(response);
	}

	public ResponseEntity<ShoppingCart> getCartByShoppingCartName(String shoppingCartName) {
		ShoppingCart shoppingCart = shoppingCartRepository.findByShoppingCartName(shoppingCartName)
				.orElseThrow(() -> new RuntimeException("Shopping cart not found"));

		return ResponseEntity.ok(shoppingCart);
	}

	public ResponseEntity<ShoppingCart> getCartById(Long shoppingCartId) {
		ShoppingCart shoppingCart = shoppingCartRepository.findById(shoppingCartId)
				.orElseThrow(() -> new RuntimeException("Shopping cart not found"));

		return ResponseEntity.ok(shoppingCart);
	}

	public ResponseEntity<List<ShoppingCart>> getAllCarts() {
		List<ShoppingCart> shoppingCarts = shoppingCartRepository.findAll();
		return ResponseEntity.ok(shoppingCarts);
	}

	public ResponseEntity<String> deleteCartById(Long shoppingCartId) {
		if (shoppingCartRepository.existsById(shoppingCartId)) {
			shoppingCartRepository.deleteById(shoppingCartId);
			return ResponseEntity.ok("Shopping Cart deleted successfully");
		} else {
			throw new RuntimeException("Shopping Cart not found in DB");
		}
	}

	public ResponseEntity<String> deleteAllCarts() {
		shoppingCartRepository.deleteAll();
		return ResponseEntity.ok("All Shopping Carts deleted successfully");
	}
}
