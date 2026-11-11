package com.ndegwacodes.ecom.order.infrastructure.secondary.repository;

import com.ndegwacodes.ecom.order.domain.order.vo.OrderStatus;
import com.ndegwacodes.ecom.order.infrastructure.secondary.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface JpaOrderRepository extends JpaRepository<OrderEntity, Long> {

  @Modifying
  @Query("UPDATE OrderEntity  order SET order.status = :orderStatus WHERE order.publicId = :orderPublicId")
  void updateStatusByPublicId(OrderStatus orderStatus, UUID orderPublicId);

  @Modifying
  @Query("UPDATE OrderEntity order SET order.status = :newStatus, order.mpesaReceiptNumber = :receiptNumber " +
    "WHERE order.publicId = :orderPublicId AND order.status = :expectedStatus")
  int updateStatusIfCurrent(UUID orderPublicId, OrderStatus expectedStatus, OrderStatus newStatus, String receiptNumber);

  Optional<OrderEntity> findByStripeSessionId(String stripeSessionId);

  Optional<OrderEntity> findByMpesaCheckoutRequestId(String mpesaCheckoutRequestId);

  Optional<OrderEntity> findByPublicIdAndUserPublicId(UUID publicId, UUID userPublicId);

  Page<OrderEntity> findAllByUserPublicId(UUID userPublicId, Pageable pageable);

}
