package com.strawberry.ecommerce.catalog.entity;

import com.strawberry.ecommerce.shop.entity.Shop;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    // WB Managed Fields
    @Column(name = "wb_nm_id", nullable = false)
    private Long wbNmId;

    @Column(name = "brand")
    private String brand;

    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "wb_vendor_code")
    private String wbVendorCode;

    @Column(name = "wb_created_at")
    private LocalDateTime wbCreatedAt;

    @Column(name = "wb_updated_at")
    private LocalDateTime wbUpdatedAt;

    @Column(name = "need_kiz")
    private Boolean needKiz;

    @Column(name = "subject_id")
    private Long subjectId;

    // Local Managed Fields
    @Column(name = "local_title", length = 500)
    private String localTitle;

    @Column(name = "local_description", columnDefinition = "TEXT")
    private String localDescription;

    @Column(name = "seo_slug", length = 500)
    private String seoSlug;

    @Column(name = "visibility", length = 50)
    private String visibility = "ACTIVE";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "local_tags", columnDefinition = "jsonb")
    private String localTags;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductCharacteristic> characteristics = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();
}
