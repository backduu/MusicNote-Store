package com.store.store.domain.entity;

import com.store.store.domain.enums.ProductStatus;
import com.store.store.domain.enums.ProductType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;


@Table(name="products")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@Getter
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 상품명
    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = true, length = 20)
    private String country;

    // 상품 설명
    @Column(nullable = false, length = 2000)
    private String description;

    // 가격
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // 상품 타입 (음반/악보)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductType type;

    // 제작자/아티스트/작곡가
    @Column(nullable = false, length = 100)
    private String creator;

    // 미리보기 URL (예: 악보 이미지, 음원 샘플)
    private String previewUrl;

    // 실제 파일 경로
    private String filePath;

    // 상품 상태 (판매중, 비공개, 품절 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    // 장르
    @Column(nullable = true, length = 20)
    private String genre;

    // 판매자 (User와 다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    // 악보 (Sheet와 일대일 관계)
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Sheet sheet;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductTag> productTags = new HashSet<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // productTags 세팅 관련 메소드
    // 개별 productTags를 추가
    public void addProductTag(ProductTag tag) {
        this.productTags.add(tag);
        tag.setProduct(this);
    }

    // 전체 productTags 세트를 교체
    public void setProductTags(Set<ProductTag> productTags) {
        this.productTags.clear();
        if (productTags != null) {
            productTags.forEach(this::addProductTag);
        }
    }

    // 생성일자 업데이트 메소드
    public void updateCreatedAt() {
        this.createdAt = LocalDateTime.now();
    }
    public void removeTag(Tag tag) {
        this.productTags.removeIf(pt -> pt.getTag().equals(tag));
    }
}
