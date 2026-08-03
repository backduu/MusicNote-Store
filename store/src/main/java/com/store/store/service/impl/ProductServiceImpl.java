package com.store.store.service.impl;

import com.store.store.component.CartMapper;
import com.store.store.component.ProductMapper;
import com.store.store.domain.entity.Cart;
import com.store.store.domain.entity.CartItem;
import com.store.store.domain.entity.Product;
import com.store.store.domain.entity.User;
import com.store.store.domain.enums.Difficulty;
import com.store.store.domain.enums.MetricType;
import com.store.store.domain.enums.ProductStatus;
import com.store.store.domain.enums.ProductType;
import com.store.store.dto.CartDTO;
import com.store.store.dto.ProductDTO;
import com.store.store.repository.CartItemRepository;
import com.store.store.repository.CartRepository;
import com.store.store.repository.ProductRepository;
import com.store.store.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service("productServiceImpl")
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductMapper productMapper;
    private final CartMapper cartMapper;

    // 금주의 새 상품 조회 (캐러셀 전용)
    @Override
    @Transactional
    public List<ProductDTO.Response> findNewProductsForCarousel(int limit) {
        // 이번 주 첫 날
        LocalDateTime startOfWeek = LocalDateTime.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        // 이번 주 막 날
        LocalDateTime endOfWeek = startOfWeek.plusDays(7);

        List<Product> products = productRepository.findNewProducts(startOfWeek, endOfWeek, ProductStatus.ONSALE);

        return products.stream()
                .limit(limit)
                .map(productMapper::toResponse)
                .collect(Collectors.toList());

    }

    // 오늘의 상품 조회
    @Override
    @Transactional
    public List<ProductDTO.Response> findTodayNewProducts() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Product> products = productRepository.findNewProducts(startOfDay, endOfDay, ProductStatus.ONSALE);

        return products.stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
    }

    // 장바구니 아이템 추가
    @Override
    @Transactional
    public CartDTO.CartResponse addItemToCart(User user, CartDTO.ItemRequest item) {
        // 사용자 장바구니 조회
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .user(user)
                                .build()
                ));

        // 상품 확인
        Product product = productRepository.findById(Math.toIntExact(item.getProductId()))
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        CartItem cartItem = cart.getItems().stream()
                .filter(i -> i.getProduct().equals(product))
                .findFirst()
                .orElseGet(() -> {
                    CartItem newItem = CartItem.builder()
                            .cart(cart)
                            .product(product)
                            .quantity(0)
                            .build();
                    cart.getItems().add(newItem);
                    return newItem;
                });

        cartItem.increaseQuantity(cartItem.getQuantity() + item.getQuantity());
        cartItemRepository.save(cartItem);

        return cartMapper.toCartResponse(cart);
    }

    // 내 장바구니 검색
    @Override
    @Transactional(readOnly = true)
    public CartDTO.CartResponse getCart(User user) {
        // 장바구니가 없으면 빈 장바구니를 내놓기
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> Cart.builder().user(user).build());

        return cartMapper.toCartResponse(cart);
    }

    // 내 장바구니 수량 조정
    @Override
    @Transactional
    public CartDTO.CartResponse updateItemQuantity(User user, Long itemId, CartDTO.ItemRequest request) {
        // 사용자 장바구니 조회
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

        // 해당 장바구니 내 상품 조회
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다."));

        // 수량 변경
        cartItem.changeQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        return cartMapper.toCartResponse(cart);
    }

    // 장바구니 내 상품 제거
    @Override
    @Transactional
    public CartDTO.CartResponse removeItemFromCart(User user, Long itemId) {
        // 사용자 장바구니 조회
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

        // 해당 장바구니 내 상품 조회
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다."));

        // 장바구니에서 제거
        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        return cartMapper.toCartResponse(cart);
    }

    // 장바구니 내 전체 상품 제거
    @Override
    @Transactional
    public CartDTO.CartResponse clearCart(User user) {
        // 사용자 장바구니 조회
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다."));

        // 모든 아이템 제거
        cart.getItems().clear();
        cartItemRepository.deleteAllByCart(cart);

        return cartMapper.toCartResponse(cart);
    }

    // 메트릭 타입 별 Top100 조회
    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO.Response> getTop100(String metricType, String period, int page, int size) {
        MetricType type = MetricType.valueOf(metricType.toUpperCase());

        // TODAY, MONTH, YEAR 별 실 기간 설정
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;
        switch (period.toUpperCase()) {
            case "TODAY" -> start = end.toLocalDate().atStartOfDay();
            case "WEEK" -> start = end.minusWeeks(1);
            case "MONTH" -> start = end.minusMonths(1);
            default -> start = LocalDateTime.MIN;
        }
        List<Product> products = productRepository.findTopProductsByMetricAndPeriod(ProductStatus.ONSALE, type, start, end, PageRequest.of(page, size));
        return products.stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getGenres() {
        return productRepository.findDistinctGenres();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDTO.Response> getSongMarketProducts(
            ProductType type, String region, String period, String sort, String genre,
            int page, int size, String searchTerm) {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;
        switch (period.toUpperCase()) {
            case "TODAY" -> start = end.toLocalDate().atStartOfDay();
            case "WEEK" -> start = end.minusWeeks(1);
            case "MONTH" -> start = end.minusMonths(1);
            case "YEAR" -> start = end.minusYears(1);
            default -> start = LocalDateTime.MIN;
        }
        Pageable pageable = PageRequest.of(page, size);

        List<Product> products;
        if (sort.equalsIgnoreCase("LIKE")) {
            products = productRepository.findSongMarketProductsByMetric(
                    ProductStatus.ONSALE, type, region, genre, start, end, MetricType.LIKE, pageable, searchTerm
            );
        } else if (sort.equalsIgnoreCase("VIEW")) {
            products = productRepository.findSongMarketProductsByMetric(
                    ProductStatus.ONSALE, type, region, genre, start, end,
                    MetricType.VIEW, pageable, searchTerm
            );
        }else {
            products = productRepository.findSongMarketProductsByReleased(
                    ProductStatus.ONSALE, type, region, genre, start, end, pageable, searchTerm);
        }

        return products.stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public List<ProductDTO.Response> getSheetArchive(ProductType type, String region, String period, String sort, String genre, int page, int size
            , String searchTerm, String instrument, Difficulty difficulty, String era) {
        // 1. 기간 계산
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;
        switch (period.toUpperCase()) {
            case "TODAY" -> start = end.toLocalDate().atStartOfDay();
            case "WEEK" -> start = end.minusWeeks(1);
            case "MONTH" -> start = end.minusMonths(1);
            case "YEAR" -> start = end.minusYears(1);
            default -> start = LocalDateTime.MIN;
        }

        Pageable pageable = PageRequest.of(page, size);

        List<Product> products;

        if (sort.equalsIgnoreCase("LIKE")) {
            products = productRepository.findSheetArchiveByMetric(
                    ProductStatus.ONSALE, type, region, genre, instrument,
                    difficulty, era, start, end, MetricType.LIKE, searchTerm, pageable
            );
        } else if (sort.equalsIgnoreCase("VIEW")) {
            products = productRepository.findSheetArchiveByMetric(
                    ProductStatus.ONSALE, type, region, genre, instrument,
                    difficulty, era, start, end, MetricType.VIEW, searchTerm, pageable
            );
        } else {
            products = productRepository.findSheetArchiveByReleased(
                    ProductStatus.ONSALE, type, region, genre, instrument,
                    difficulty, era, start, end, searchTerm, pageable
            );
        }

        return products.stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public List<ProductDTO.Response> searchProducts(String keyword, int page, int size) {


        return List.of();
    }


}
