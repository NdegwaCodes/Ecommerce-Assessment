package com.ndegwacodes.ecom.product.infrastructure.secondary.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndegwacodes.ecom.product.infrastructure.secondary.entity.CategoryEntity;
import com.ndegwacodes.ecom.product.infrastructure.secondary.entity.CategoryEntityBuilder;
import com.ndegwacodes.ecom.product.infrastructure.secondary.entity.PictureEntityBuilder;
import com.ndegwacodes.ecom.product.infrastructure.secondary.entity.ProductEntity;
import com.ndegwacodes.ecom.product.infrastructure.secondary.entity.ProductEntityBuilder;
import com.ndegwacodes.ecom.product.infrastructure.secondary.repository.JpaCategoryRepository;
import com.ndegwacodes.ecom.product.infrastructure.secondary.repository.JpaProductPictureRepository;
import com.ndegwacodes.ecom.product.infrastructure.secondary.repository.JpaProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "application.demo-data.enabled", havingValue = "true")
public class DemoCatalogueSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DemoCatalogueSeeder.class);
  private static final String CATALOGUE = "demo/catalogue.json";

  private final JpaCategoryRepository categoryRepository;
  private final JpaProductRepository productRepository;
  private final JpaProductPictureRepository pictureRepository;
  private final ObjectMapper objectMapper;

  public DemoCatalogueSeeder(JpaCategoryRepository categoryRepository,
                             JpaProductRepository productRepository,
                             JpaProductPictureRepository pictureRepository,
                             ObjectMapper objectMapper) {
    this.categoryRepository = categoryRepository;
    this.productRepository = productRepository;
    this.pictureRepository = pictureRepository;
    this.objectMapper = objectMapper;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) throws IOException {
    if (productRepository.count() > 0) {
      log.info("Catalogue already has products, demo data not loaded");
      return;
    }

    DemoCatalogue catalogue;
    try (InputStream json = new ClassPathResource(CATALOGUE).getInputStream()) {
      catalogue = objectMapper.readValue(json, DemoCatalogue.class);
    }

    Map<String, CategoryEntity> categories = new HashMap<>();
    for (CategoryEntity existing : categoryRepository.findAll()) {
      categories.put(existing.getName(), existing);
    }
    for (String name : catalogue.categories()) {
      categories.computeIfAbsent(name, missing -> categoryRepository.save(
        CategoryEntityBuilder.categoryEntity()
          .name(missing)
          .publicId(UUID.randomUUID())
          .products(new HashSet<>())
          .build()));
    }

    for (DemoProduct demo : catalogue.products()) {
      ProductEntity product = productRepository.save(ProductEntityBuilder.productEntity()
        .name(demo.name())
        .brand(demo.brand())
        .description(demo.description())
        .price(demo.price())
        .wattage(demo.wattage())
        .warrantyYears(demo.warrantyYears())
        .featured(demo.featured())
        .nbInStock(demo.stock())
        .publicId(UUID.randomUUID())
        .category(categories.get(demo.category()))
        .pictures(new HashSet<>())
        .build());

      pictureRepository.save(PictureEntityBuilder.pictureEntity()
        .file(new ClassPathResource(demo.picture()).getContentAsByteArray())
        .mimeType("image/svg+xml")
        .product(product)
        .build());
    }

    log.info("Loaded {} demo products in {} categories", catalogue.products().size(), catalogue.categories().size());
  }

  record DemoCatalogue(List<String> categories, List<DemoProduct> products) {
  }

  record DemoProduct(String picture, String name, String brand, String category, double price,
                     int wattage, int warrantyYears, boolean featured, int stock, String description) {
  }
}
