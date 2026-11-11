package com.ndegwacodes.ecom.product.infrastructure.primary;

import com.ndegwacodes.ecom.product.application.ProductsApplicationService;
import com.ndegwacodes.ecom.product.domain.aggregate.FilterQueryBuilder;
import com.ndegwacodes.ecom.product.domain.aggregate.Product;
import com.ndegwacodes.ecom.product.domain.vo.PublicId;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/products-shop")
public class ProductsShopResource {

  private final ProductsApplicationService productsApplicationService;

  public ProductsShopResource(ProductsApplicationService productsApplicationService) {
    this.productsApplicationService = productsApplicationService;
  }

  @GetMapping("/featured")
  public ResponseEntity<Page<RestProduct>> getAllFeatured(Pageable pageable) {
    Page<Product> products = productsApplicationService.getFeaturedProducts(pageable);

    PageImpl<RestProduct> restProducts = new PageImpl<>(
      products.getContent().stream().map(RestProduct::fromDomain).toList(),
      pageable,
      products.getTotalElements()
    );
    return ResponseEntity.ok(restProducts);
  }

  @GetMapping("/find-one")
  public ResponseEntity<RestProduct> getOne(@RequestParam("publicId") UUID id) {
    Optional<Product> productOpt = productsApplicationService.findOne(new PublicId(id));

    return productOpt.map(product -> ResponseEntity.ok(RestProduct.fromDomain(product)))
      .orElseGet(() -> ResponseEntity.badRequest().build());
  }

  @GetMapping("/related")
  public ResponseEntity<Page<RestProduct>> findRelated(Pageable pageable,
                                                       @RequestParam("publicId") UUID id) {
    try {
      Page<Product> products = productsApplicationService.findRelated(pageable, new PublicId(id));
      PageImpl<RestProduct> restProducts = new PageImpl<>(
        products.getContent().stream().map(RestProduct::fromDomain).toList(),
        pageable,
        products.getTotalElements()
      );
      return ResponseEntity.ok(restProducts);
    } catch (EntityNotFoundException enfe) {
      return ResponseEntity.badRequest().build();
    }
  }

  @GetMapping("/filter")
  public ResponseEntity<Page<RestProduct>> filter(Pageable pageable,
                                                  @RequestParam("categoryId") UUID categoryId,
                                                  @RequestParam(value = "minWattage", required = false) Integer minWattage,
                                                  @RequestParam(value = "maxWattage", required = false) Integer maxWattage) {
    FilterQueryBuilder filterQueryBuilder = FilterQueryBuilder.filterQuery().categoryId(new PublicId(categoryId));

    filterQueryBuilder.minWattage(minWattage).maxWattage(maxWattage);

    Page<Product> products = productsApplicationService.filter(pageable, filterQueryBuilder.build());
    PageImpl<RestProduct> restProducts = new PageImpl<>(
      products.getContent().stream().map(RestProduct::fromDomain).toList(),
      pageable,
      products.getTotalElements()
    );
    return ResponseEntity.ok(restProducts);
  }
}
