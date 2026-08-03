package com.store.store.controller;

import com.store.store.domain.entity.User;
import com.store.store.domain.enums.Difficulty;
import com.store.store.domain.enums.ProductType;
import com.store.store.dto.CartDTO;
import com.store.store.dto.ProductDTO;
import com.store.store.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Product API", description = "상품(악보/음원/음반) 관련 API (금주의 추천 아이템, 신규 아이템 등")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @GetMapping("/global-search")
    @Operation(summary = "상품 통합 검색", description = "모든 상품(ONSALE/SOLDOUT)을 검색합니다.")
    public ResponseEntity<List<ProductDTO.Response>> searchProducts(
            @RequestParam(required = true) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        return ResponseEntity.ok(productService.searchProducts(keyword, page, size));
    }

    @Operation(summary = "캐루셀 음원 조회", description = "상품이 'ONSALE'인 상태의 금주에 나온 상품을 조회합니다.")
    @GetMapping("/carousel/new")
    public ResponseEntity<List<ProductDTO.Response>> getWeeklyNewProducts(
        @RequestParam(defaultValue = "15") int limit
    ) {
        return ResponseEntity.ok(productService.findNewProductsForCarousel(limit));
    }

    @GetMapping("/new")
    @Operation(summary = "오늘 정식 발행된 상품 조회", description = "상품이 'ONSALE'인 상태의 오늘 출시된 상품을 조회합니다.")
    public ResponseEntity<List<ProductDTO.Response>> getTodayNewProducts() {
        return ResponseEntity.ok(productService.findTodayNewProducts());
    }

    @GetMapping("/top100")
    @Operation(summary = "기간과 조회수/좋아요 기준 음악 TOP 100 조회", description = "상품이 'ONSALE'인 상태의 기간 및 metricType 별 TOP 100을 추려내어 조회합니다.")
    public ResponseEntity<List<ProductDTO.Response>> getTop100(
            @RequestParam(defaultValue = "VIEW") String metricType,
            @RequestParam(defaultValue = "MONTH") String period,
            @RequestParam(defaultValue = "0") String page,
            @RequestParam(defaultValue = "15") String size
    ) {
        List<ProductDTO.Response> response = productService.getTop100(metricType, period, Integer.parseInt(page), Integer.parseInt(size));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/songMarket")
    @Operation(summary = "음원/음반 조회", description = "기간/타입/국가/정렬/장르 별 'ONSALE'인 상품을 추려내어 조회합니다.")
    public ResponseEntity<List<ProductDTO.Response>> getSongMarket(
            @RequestParam(defaultValue = "SONG") ProductType type,
            @RequestParam(defaultValue = "Korea") String region,
            @RequestParam(defaultValue = "MONTH") String period,
            @RequestParam(defaultValue = "RELEASED") String sort,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "0") String page,
            @RequestParam(defaultValue = "15") String size,
            @RequestParam(defaultValue = "") String searchTerm
    ) {
        List<ProductDTO.Response> response = productService.getSongMarketProducts(type, region, period, sort, genre, Integer.parseInt(page), Integer.parseInt(size), searchTerm);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sheet")
    @Operation(summary = "악보 조회", description = "기간/타입/국가/정렬/장르/세션/난이도/시대 별 'ONSALE'인 상품을 추려내어 조회합니다.")
    public ResponseEntity<List<ProductDTO.Response>> getSheet(
            @RequestParam(defaultValue = "SHEET") ProductType type,
            @RequestParam(defaultValue = "Korea") String region,
            @RequestParam(defaultValue = "MONTH") String period,
            @RequestParam(defaultValue = "RELEASED") String sort,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "0") String page,
            @RequestParam(defaultValue = "15") String size,
            @RequestParam(defaultValue = "") String searchTerm,
            @RequestParam(defaultValue = "") String instrument,
            @RequestParam(defaultValue = "") Difficulty difficulty,
            @RequestParam(defaultValue = "") String era
    ) {
        List<ProductDTO.Response> response = productService.getSheetArchive(type, region, period, sort, genre
                                                                           , Integer.parseInt(page), Integer.parseInt(size)
                                                                           , searchTerm, instrument, difficulty, era);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cart/items")
    @Operation(summary = "장바구니에 상품 추가", description = "상품을 장바구니에 담습니다.")
    public ResponseEntity<CartDTO.CartResponse> addCartItems(
            @RequestBody CartDTO.ItemRequest request
            , @AuthenticationPrincipal User user
    ) {
        CartDTO.CartResponse response = productService.addItemToCart(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cart/myCart")
    @Operation(summary = "내 장바구니 검색", description = "나의 장바구니를 조회합니다.")
    public ResponseEntity<CartDTO.CartResponse> getCart(
            @AuthenticationPrincipal User user
    ) {
        CartDTO.CartResponse response = productService.getCart(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cart/items/{itemId}")
    @Operation(summary = "내 장바구니의 상품 수량 변경", description = "나의 장바구니에 담긴 상품의 수량을 조정합니다.")
    public ResponseEntity<CartDTO.CartResponse> updateCartItemQuantity(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId,
            @RequestBody CartDTO.ItemRequest request
    ) {
        CartDTO.CartResponse response = productService.updateItemQuantity(user, itemId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/cart/items/{itemId}")
    @Operation(summary = "내 장바구니의 상품 삭제", description = "나의 장바구니에 담긴 상품을 삭제합니다.")
    public ResponseEntity<CartDTO.CartResponse> removeCartItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long itemId
    ) {
        CartDTO.CartResponse response = productService.removeItemFromCart(user, itemId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/cart/items/clear")
    @Operation(summary = "내 장바구니의 상품 전체 삭제", description = "나의 장바구니에 담긴 상품을 전체 삭제합니다.")
    public ResponseEntity<CartDTO.CartResponse> clearCart(
            @AuthenticationPrincipal User user
    ) {
        CartDTO.CartResponse response = productService.clearCart(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/genres/song")
    public ResponseEntity<List<String>> getGenresSong(){
        List<String> genres = productService.getGenres();
        return ResponseEntity.ok(genres);
    }

    @GetMapping("/genres/sheet")
    public ResponseEntity<List<String>> getGenresSheet(){
        List<String> genres = productService.getGenres();
        return ResponseEntity.ok(genres);
    }


}
