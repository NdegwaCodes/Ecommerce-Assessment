package com.ndegwacodes.ecom.product.infrastructure.secondary.repository;

import com.ndegwacodes.ecom.product.infrastructure.secondary.entity.PictureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductPictureRepository extends JpaRepository<PictureEntity, Long> {
}
