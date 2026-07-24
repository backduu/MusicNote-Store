package com.store.store.config;

import com.store.store.domain.entity.Cart;
import com.store.store.domain.entity.CartItem;
import com.store.store.domain.entity.Product;
import com.store.store.domain.entity.User;
import com.store.store.domain.enums.ProductStatus;
import com.store.store.domain.enums.ProductType;
import com.store.store.domain.enums.UserRole;
import com.store.store.domain.enums.UserStatus;
import com.store.store.repository.CartItemRepository;
import com.store.store.repository.CartRepository;
import com.store.store.repository.ProductRepository;
import com.store.store.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@Profile("dev")
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public DataInitializer(UserRepository userRepository,
                                 ProductRepository productRepository,
                                 CartRepository cartRepository,
                                 CartItemRepository cartItemRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
      log.info("[SYSTEM] 로컬 개발용 초기 데이터 주입");

        User savedUser = userRepository.findByUsername("music_lover")
                        .map(existedUser -> {
                            existedUser.initUser(
                                    "lover@musicnote.com",
                                    "홍길동",
                                    "음악대장",
                                    "01012345678",
                                    UserRole.USER,
                                    UserStatus.ACTIVE
                            );

                            return existedUser;
                        })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .username("music_lover")
                                .email("lover@musicnote.com")
                                .name("홍길동")
                                .nickname("음악대장")
                                .password("hashed_password_here")
                                .phone("01012345678")
                                .role(UserRole.USER)
                                .status(UserStatus.ACTIVE)
                                .created(LocalDateTime.now())
                                .approvedAt(LocalDateTime.now())
                                .build()
                ));

        /* ------------------------------------------------------------
         * 2. PRODUCTS (상품) 30개 데이터 생성
         * ------------------------------------------------------------ */
        List<Product> productsToSave = new ArrayList<>();

        // 프론트엔드 UI 테스트를 위한 리얼한 샘플 데이터 배열
        String[] creators = {"아이유(IU)", "Chopin", "DAY6", "히사이시 조", "백예린", "윤하", "Beethoven", "NewJeans", "태연", "성시경"};
        String[] genres = {"K-POP", "CLASSIC", "K-POP", "OST", "INDIE", "POP", "CLASSIC", "K-POP", "POP", "BALLAD"};
        String[] types = {"ALBUM", "SHEET", "ALBUM", "SHEET", "ALBUM", "SHEET", "SHEET", "ALBUM", "ALBUM", "SHEET"};
        int[] basePrices = {28500, 3500, 24000, 4000, 31000, 5000, 3000, 26000, 29000, 4500};

        for (int i = 1; i <= 30; i++) {
            int idx = (i - 1) % 10;
            String type = types[idx];
            String titleSuffix = type.equals("ALBUM") ? " " + (i/10 + 1) + "집 앨범 [Vol." + i + "]" : " 피아노 연주곡 악보 Vol." + i;
            String title = creators[idx] + titleSuffix;

            // 이미 존재하는 상품은 건너뜀
            if (productRepository.existsByTitle(title)) {
                continue;
            }

            Product product = Product.builder()
                    .title(title)
                    .price(new BigDecimal(basePrices[idx] + (i * 100))) // 가격에 조금씩 차이를 둠
                    .creator(creators[idx])
                    .genre(genres[idx])
                    .type(ProductType.valueOf(type))
                    .status(ProductStatus.valueOf("ONSALE"))
                    .description(creators[idx] + "의 최고의 감성을 담은 " + (type.equals("ALBUM") ? "명반" : "악보") + "입니다.")
                    .seller(savedUser) // 1순위 회원을 판매자로 매핑
                    .createdAt(LocalDateTime.now().minusDays(30 - i)) // 최신순 정렬 테스트를 위해 날짜 분산
                    .build();

            productsToSave.add(product);
        }

        // 30개 상품 일괄 저장
        if (!productsToSave.isEmpty()) {
            productRepository.saveAll(productsToSave);
            System.out.println("[System] " + productsToSave.size() + "개의 신규 상품 데이터 세이브 완료!");
        }

        List<Product> savedProducts = productRepository.findAll();

        /* ------------------------------------------------------------
         * 3. CART (장바구니) 생성 - 존재 여부 확인 후 처리
         * ------------------------------------------------------------ */
        Cart savedCart = cartRepository.findByUser(savedUser)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .user(savedUser)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()
                ));

        /* ------------------------------------------------------------
         * 4. CART_ITEMS 장바구니에 3건 담기 - 기존 아이템 삭제 후 새로 담기
         * ------------------------------------------------------------ */
        cartItemRepository.deleteAllByCart(savedCart);

        // 방금 생성된 30개 상품 중 1번, 5번, 10번 상품을 장바구니에 담기
        Product pick1 = savedProducts.get(0);
        Product pick2 = savedProducts.get(4);
        Product pick3 = savedProducts.get(9);

        CartItem item1 = CartItem.builder()
                .cart(savedCart)
                .product(pick1)
                .quantity(2)
                .priceSnapshot(pick1.getPrice())
                .createdAt(LocalDateTime.now())
                .build();

        CartItem item2 = CartItem.builder()
                .cart(savedCart)
                .product(pick2)
                .quantity(1)
                .priceSnapshot(pick2.getPrice())
                .createdAt(LocalDateTime.now())
                .build();


        cartItemRepository.saveAll(List.of(item1, item2));


        log.info("[SYSTEM] 로컬 개발용 초기 데이터 주입 완료");
    }
}
