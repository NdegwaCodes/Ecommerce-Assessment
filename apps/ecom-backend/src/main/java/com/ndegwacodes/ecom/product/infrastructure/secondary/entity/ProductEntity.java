package com.ndegwacodes.ecom.product.infrastructure.secondary.entity;

import com.ndegwacodes.ecom.product.domain.aggregate.Product;
import com.ndegwacodes.ecom.product.domain.aggregate.ProductBuilder;
import com.ndegwacodes.ecom.product.domain.vo.*;
import com.ndegwacodes.ecom.shared.jpa.AbstractAuditingEntity;
import jakarta.persistence.*;
import org.jilt.Builder;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "product")
@Builder
public class ProductEntity extends AbstractAuditingEntity<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "productSequence")
  @SequenceGenerator(name = "productSequence", sequenceName = "product_sequence", allocationSize = 1)
  @Column(name = "id")
  private Long id;


  @Column(name = "brand")
  private String brand;

  @Column(name = "warranty_years")
  private int warrantyYears;

  @Column(name = "description")
  private String description;

  @Column(name = "name")
  private String name;

  @Column(name = "price")
  private double price;

  @Column(name = "featured")
  private boolean featured;

  @Column(name = "wattage")
  private int wattage;

  @Column(name = "publicId", unique = true)
  private UUID publicId;

  @Column(name = "nb_in_stock")
  private int nbInStock;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "product")
  private Set<PictureEntity> pictures = new HashSet<>();

  @ManyToOne
  @JoinColumn(name = "category_fk", referencedColumnName = "id")
  private CategoryEntity category;

  public ProductEntity() {
  }

  public ProductEntity(Long id, String brand, int warrantyYears, String description, String name, double price, boolean featured, int wattage, UUID publicId, int nbInStock, Set<PictureEntity> pictures, CategoryEntity category) {
    this.id = id;
    this.brand = brand;
    this.warrantyYears = warrantyYears;
    this.description = description;
    this.name = name;
    this.price = price;
    this.featured = featured;
    this.wattage = wattage;
    this.publicId = publicId;
    this.nbInStock = nbInStock;
    this.pictures = pictures;
    this.category = category;
  }

  public static ProductEntity from(Product product) {
    ProductEntityBuilder productEntityBuilder = ProductEntityBuilder.productEntity();

    if(product.getDbId() != null) {
      productEntityBuilder.id(product.getDbId());
    }

    return productEntityBuilder
      .brand(product.getProductBrand().value())
      .warrantyYears(product.getWarranty().years())
      .description(product.getDescription().value())
      .name(product.getName().value())
      .price(product.getPrice().value())
      .wattage(product.getWattage().value())
      .publicId(product.getPublicId().value())
      .category(CategoryEntity.from(product.getCategory()))
      .pictures(PictureEntity.from(product.getPictures()))
      .featured(product.getFeatured())
      .nbInStock(product.getNbInStock())
      .build();
  }

  public static Product to(ProductEntity productEntity) {
    return ProductBuilder.product()
      .productBrand(new ProductBrand(productEntity.getBrand()))
      .warranty(new ProductWarranty(productEntity.getWarrantyYears()))
      .description(new ProductDescription(productEntity.getDescription()))
      .name(new ProductName(productEntity.getName()))
      .price(new ProductPrice(productEntity.getPrice()))
      .wattage(new ProductWattage(productEntity.getWattage()))
      .publicId(new PublicId(productEntity.getPublicId()))
      .category(CategoryEntity.to(productEntity.getCategory()))
      .pictures(PictureEntity.to(productEntity.getPictures()))
      .featured(productEntity.getFeatured())
      .nbInStock(productEntity.getNbInStock())
      .build();
  }

  @Override
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public int getWarrantyYears() {
    return warrantyYears;
  }

  public void setWarrantyYears(int warrantyYears) {
    this.warrantyYears = warrantyYears;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getPrice() {
    return price;
  }

  public void setPrice(double price) {
    this.price = price;
  }

  public boolean getFeatured() {
    return featured;
  }

  public void setFeatured(boolean featured) {
    this.featured = featured;
  }

  public int getWattage() {
    return wattage;
  }

  public void setWattage(int wattage) {
    this.wattage = wattage;
  }

  public UUID getPublicId() {
    return publicId;
  }

  public void setPublicId(UUID publicId) {
    this.publicId = publicId;
  }

  public int getNbInStock() {
    return nbInStock;
  }

  public void setNbInStock(int nbInStock) {
    this.nbInStock = nbInStock;
  }

  public Set<PictureEntity> getPictures() {
    return pictures;
  }

  public void setPictures(Set<PictureEntity> pictures) {
    this.pictures = pictures;
  }

  public CategoryEntity getCategory() {
    return category;
  }

  public void setCategory(CategoryEntity category) {
    this.category = category;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProductEntity that)) return false;
    return featured == that.featured && Objects.equals(brand, that.brand) && warrantyYears == that.warrantyYears && Objects.equals(description, that.description) && Objects.equals(name, that.name) && Objects.equals(price, that.price) && wattage == that.wattage && Objects.equals(publicId, that.publicId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(brand, warrantyYears, description, name, price, featured, wattage, publicId);
  }
}
