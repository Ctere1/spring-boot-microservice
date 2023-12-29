package com.techcareer.shoppingcartservice.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Entity
public class ShoppingCart {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(unique = true)
	private String shoppingCartName;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getShoppingCartName() {
		return shoppingCartName;
	}

	public void setShoppingCartName(String shoppingCartName) {
		this.shoppingCartName = shoppingCartName;
	}

	public Set<Product> getProducts() {
		return products;
	}

	public void setProducts(Set<Product> products) {
		this.products = products;
	}

	@ManyToMany
	@JoinTable(name = "shopping_cart_product", joinColumns = @JoinColumn(name = "shopping_cart_id"), inverseJoinColumns = @JoinColumn(name = "product_id"))
	Set<Product> products;
}
