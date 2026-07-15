package com.store.store.repository;

import com.store.store.domain.entity.Cart;
import com.store.store.domain.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    void deleteAllByCart(Cart cart);
}
