package com.kurly.tet.guide.springrestdocs.domain.product;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductDto {
    private Long id;
    private String productName;
    private String productNo;
    private ProductStatus productStatus;
    private LocalDateTime created;
    private LocalDateTime modified;

    public ProductDto(Long id, String productName, String productNo) {
        this.id = id;
        this.productName = productName;
        this.productNo = productNo;
        this.productStatus = ProductStatus.CREATED;

        LocalDateTime now = LocalDateTime.now();
        this.created = now;
        this.modified = now;
    }

    public String getProductStatusDescription() {
        return getProductStatus().getDescription();
    }

    public void modify(@NotBlank String productName, @NotBlank String productNo, @NotNull ProductStatus productStatus) {
        this.productName = productName;
        this.productNo = productNo;
        this.productStatus = productStatus;
        this.modified = LocalDateTime.now();
    }
}
