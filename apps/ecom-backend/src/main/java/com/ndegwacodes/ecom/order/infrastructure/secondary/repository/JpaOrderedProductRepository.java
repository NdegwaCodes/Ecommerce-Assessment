package com.ndegwacodes.ecom.order.infrastructure.secondary.repository;

import com.ndegwacodes.ecom.order.infrastructure.secondary.entity.OrderedProductEntity;
import com.ndegwacodes.ecom.order.infrastructure.secondary.entity.OrderedProductEntityPk;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOrderedProductRepository extends JpaRepository<OrderedProductEntity, OrderedProductEntityPk> {

}
