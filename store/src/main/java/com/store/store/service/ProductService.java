package com.store.store.service;

import com.store.store.domain.entity.User;
import com.store.store.domain.enums.Difficulty;
import com.store.store.domain.enums.ProductType;
import com.store.store.dto.CartDTO;
import com.store.store.dto.ProductDTO;

import java.util.List;

public interface ProductService {
    List<ProductDTO.Response> findNewProductsForCarousel(int limit);

    List<ProductDTO.Response> findTodayNewProducts();

    CartDTO.CartResponse addItemToCart(User user, CartDTO.ItemRequest item);

    CartDTO.CartResponse getCart(User user);

    CartDTO.CartResponse updateItemQuantity(User user, Long itemId, CartDTO.ItemRequest request);

    CartDTO.CartResponse removeItemFromCart(User user, Long itemId);

    CartDTO.CartResponse clearCart(User user);

    List<ProductDTO.Response> getTop100(String metricType, String period, int page, int size);

    List<String> getGenres();

    List<ProductDTO.Response> getSongMarketProducts(ProductType type, String region, String period, String sort, String genre,
                                                    int page, int size, String searchTerm);

    List<ProductDTO.Response> getSheetArchive(ProductType type, String region, String period, String sort, String genre, int page, int size
                                            , String searchTerm, String instrument, Difficulty difficulty, String era);

    List<ProductDTO.Response> searchProducts(String keyword, int page, int size);
}

