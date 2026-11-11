package com.ndegwacodes.ecom.product.infrastructure.secondary.demo;

import com.ndegwacodes.ecom.product.infrastructure.secondary.entity.ProductEntity;
import com.ndegwacodes.ecom.product.infrastructure.secondary.repository.JpaCategoryRepository;
import com.ndegwacodes.ecom.product.infrastructure.secondary.repository.JpaProductPictureRepository;
import com.ndegwacodes.ecom.product.infrastructure.secondary.repository.JpaProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "application.demo-data.enabled=true")
class DemoCatalogueSeederTest {

  @Autowired
  private JpaProductRepository productRepository;

  @Autowired
  private JpaCategoryRepository categoryRepository;

  @Autowired
  private JpaProductPictureRepository pictureRepository;

  @Test
  void loadsTheSolarCatalogueWithAPicturePerProduct() {
    List<ProductEntity> products = productRepository.findAll();
    assertThat(products).hasSize(11);
    assertThat(categoryRepository.count()).isGreaterThanOrEqualTo(5);
    assertThat(pictureRepository.count()).isEqualTo(11);
    assertThat(products).allSatisfy(product -> {
      assertThat(product.getWattage()).isPositive();
      assertThat(product.getPrice()).isGreaterThan(0);
      assertThat(product.getCategory()).isNotNull();
    });
    assertThat(products).filteredOn(ProductEntity::getFeatured).hasSize(6);
  }
}
