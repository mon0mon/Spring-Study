package com.kurly.tet.guide.springrestdocs.application.product;

import com.kurly.tet.guide.springrestdocs.domain.exception.ProductNotFoundException;
import com.kurly.tet.guide.springrestdocs.domain.product.ProductDto;
import com.kurly.tet.guide.springrestdocs.infrastructure.web.common.dto.PageRequest;
import com.kurly.tet.guide.springrestdocs.infrastructure.web.common.dto.PageResponse;
import com.kurly.tet.guide.springrestdocs.infrastructure.web.product.ProductCreateCommand;
import com.kurly.tet.guide.springrestdocs.infrastructure.web.product.ProductModifyCommand;
import com.kurly.tet.guide.springrestdocs.infrastructure.web.product.ProductSearchCondition;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 대충...
 */
@Component
public class ProductFacade {
    private final AtomicLong productIdCreator = new AtomicLong(0);
    private final List<ProductDto> products = new ArrayList<>();

    @PostConstruct
    public void setUp() {
        products.add(new ProductDto(productIdCreator.incrementAndGet(), "TEST" + productIdCreator.incrementAndGet(), String.format("%05d", productIdCreator.incrementAndGet())));
        products.add(new ProductDto(productIdCreator.incrementAndGet(), "TEST" + productIdCreator.incrementAndGet(), String.format("%05d", productIdCreator.incrementAndGet())));
        products.add(new ProductDto(productIdCreator.incrementAndGet(), "TEST" + productIdCreator.incrementAndGet(), String.format("%05d", productIdCreator.incrementAndGet())));
        products.add(new ProductDto(productIdCreator.incrementAndGet(), "TEST" + productIdCreator.incrementAndGet(), String.format("%05d", productIdCreator.incrementAndGet())));
        products.add(new ProductDto(productIdCreator.incrementAndGet(), "TEST" + productIdCreator.incrementAndGet(), String.format("%05d", productIdCreator.incrementAndGet())));
    }

    public PageResponse search(ProductSearchCondition searchCondition, PageRequest pageRequest) {
        var filteredProducts = this.products.stream()
                .filter(searchCondition::filter)
                .collect(Collectors.toList());;

        return new PageResponse<>(filteredProducts, pageRequest, this.products.size());
    }

    public ProductDto create(ProductCreateCommand createCommand) {
        var createProduct = createCommand.createDto(productIdCreator.incrementAndGet());

        products.add(createProduct);

        return createProduct;
    }

    public ProductDto getProduct(Long productId) {
        return products.stream()
                .filter(it -> it.getId().equals(productId)).findFirst()
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    public ProductDto modify(Long productId, ProductModifyCommand modifyCommand) {
        var targetProduct = getProduct(productId);
        modifyCommand.modify(targetProduct);

        return targetProduct;
    }

    public void remove(Long productId) {
        products.removeIf(it -> it.getId().equals(productId));
    }
}
