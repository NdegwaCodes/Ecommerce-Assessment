package com.ndegwacodes.ecom.product.infrastructure.secondary.repository;

import com.ndegwacodes.ecom.product.domain.aggregate.Product;
import com.ndegwacodes.ecom.product.infrastructure.secondary.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaProductRepository  extends JpaRepository<ProductEntity, Long> {

  int deleteByPublicId(UUID publicId);

  Optional<ProductEntity> findByPublicId(UUID publicId);

  Page<ProductEntity> findAllByFeaturedTrue(Pageable pageable);

  Page<ProductEntity> findByCategoryPublicIdAndPublicIdNot(Pageable pageable, UUID categoryPublicId, UUID excludedProductPublicId);

  @Query("SELECT product FROM ProductEntity product WHERE (:minWattage is null or product.wattage >= :minWattage) AND " +
    "(:maxWattage is null or product.wattage <= :maxWattage) AND " +
    "product.category.publicId = :categoryPublicId")
  Page<ProductEntity> findByCategoryPublicIdAndWattageBetween(Pageable pageable, UUID categoryPublicId,
                                                               Integer minWattage, Integer maxWattage);

  List<ProductEntity> findAllByPublicIdIn(List<UUID> publicIds);

  @Modifying
  @Query("UPDATE ProductEntity  product " +
    "SET product.nbInStock = product.nbInStock - :quantity " +
    "WHERE product.publicId = :productPublicId")
  void updateQuantity(UUID productPublicId, long quantity);

}
