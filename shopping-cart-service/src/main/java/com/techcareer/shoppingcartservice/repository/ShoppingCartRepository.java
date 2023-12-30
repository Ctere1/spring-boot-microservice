package com.techcareer.shoppingcartservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techcareer.shoppingcartservice.entity.ShoppingCart;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

	Optional<ShoppingCart> findById(Long id);

    Optional<ShoppingCart> findByShoppingCartName(String shoppingCartName);
}
