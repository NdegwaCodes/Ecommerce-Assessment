package com.ndegwacodes.ecom.product.domain.aggregate;

import com.ndegwacodes.ecom.product.domain.vo.*;
import com.ndegwacodes.ecom.shared.error.domain.Assert;
import org.jilt.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public class Product {

  private final ProductBrand productBrand;
  private final ProductWarranty warranty;
  private final ProductDescription description;
  private final ProductName name;
  private final ProductPrice price;
  private final ProductWattage wattage;
  private final Category category;
  private final List<Picture> pictures;
  private Long dbId;
  private boolean featured;
  private PublicId publicId;
  private int nbInStock;

  public Product(ProductBrand productBrand, ProductWarranty warranty, ProductDescription description, ProductName name, ProductPrice price, ProductWattage wattage, Category category, List<Picture> pictures, Long dbId, boolean featured, PublicId publicId, int nbInStock) {
    this.productBrand = productBrand;
    this.warranty = warranty;
    this.description = description;
    this.name = name;
    this.price = price;
    this.wattage = wattage;
    this.category = category;
    this.pictures = pictures;
    this.dbId = dbId;
    this.featured = featured;
    this.publicId = publicId;
    this.nbInStock = nbInStock;
  }

  private void assertMandatoryFields(ProductBrand brand,
                                     ProductWarranty warranty,
                                     ProductDescription description,
                                     ProductName name,
                                     ProductPrice price,
                                     ProductWattage wattage,
                                     Category category,
                                     List<Picture> pictures,
                                     boolean featured,
                                     int nbInStock) {
    Assert.notNull("brand", brand);
    Assert.notNull("warranty", warranty);
    Assert.notNull("description", description);
    Assert.notNull("name", name);
    Assert.notNull("price", price);
    Assert.notNull("wattage", wattage);
    Assert.notNull("category", category);
    Assert.notNull("pictures", pictures);
    Assert.notNull("featured", featured);
    Assert.notNull("nbInStock", nbInStock);
  }

  public void initDefaultFields() {
    this.publicId = new PublicId(UUID.randomUUID());
  }

  public ProductBrand getProductBrand() {
    return productBrand;
  }

  public ProductWarranty getWarranty() {
    return warranty;
  }

  public ProductDescription getDescription() {
    return description;
  }

  public ProductName getName() {
    return name;
  }

  public ProductPrice getPrice() {
    return price;
  }

  public ProductWattage getWattage() {
    return wattage;
  }

  public Category getCategory() {
    return category;
  }

  public List<Picture> getPictures() {
    return pictures;
  }

  public Long getDbId() {
    return dbId;
  }

  public boolean getFeatured() {
    return featured;
  }

  public PublicId getPublicId() {
    return publicId;
  }

  public int getNbInStock() {
    return nbInStock;
  }
}
