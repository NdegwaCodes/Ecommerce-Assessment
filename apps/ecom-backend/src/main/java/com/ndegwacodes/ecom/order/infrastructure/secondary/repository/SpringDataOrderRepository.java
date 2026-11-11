package com.ndegwacodes.ecom.order.infrastructure.secondary.repository;

import com.ndegwacodes.ecom.order.domain.order.aggregate.Order;
import com.ndegwacodes.ecom.order.domain.order.aggregate.StripeSessionInformation;
import com.ndegwacodes.ecom.order.domain.order.repository.OrderRepository;
import com.ndegwacodes.ecom.order.domain.order.vo.OrderStatus;
import com.ndegwacodes.ecom.order.domain.user.vo.UserPublicId;
import com.ndegwacodes.ecom.order.infrastructure.secondary.entity.OrderEntity;
import com.ndegwacodes.ecom.product.domain.vo.PublicId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SpringDataOrderRepository implements OrderRepository {

  private final JpaOrderRepository jpaOrderRepository;
  private final JpaOrderedProductRepository jpaOrderedProductRepository;

  public SpringDataOrderRepository(JpaOrderRepository jpaOrderRepository,
                                   JpaOrderedProductRepository jpaOrderedProductRepository) {
    this.jpaOrderRepository = jpaOrderRepository;
    this.jpaOrderedProductRepository = jpaOrderedProductRepository;
  }

  @Override
  public void save(Order order) {
    OrderEntity orderEntityToCreate = OrderEntity.from(order);
    OrderEntity orderSavedEntity = jpaOrderRepository.save(orderEntityToCreate);

    orderSavedEntity.getOrderedProducts()
      .forEach(orderedProductEntity -> orderedProductEntity.getId().setOrder(orderSavedEntity));
    jpaOrderedProductRepository.saveAll(orderSavedEntity.getOrderedProducts());
  }

  @Override
  public void updateStatusByPublicId(OrderStatus orderStatus, PublicId orderPublicId) {
    jpaOrderRepository.updateStatusByPublicId(orderStatus, orderPublicId.value());
  }

  @Override
  public Optional<Order> findByStripeSessionId(StripeSessionInformation stripeSessionInformation) {
    return jpaOrderRepository.findByStripeSessionId(stripeSessionInformation.stripeSessionId().value())
      .map(OrderEntity::toDomain);
  }

  @Override
  public Optional<Order> findByMpesaCheckoutRequestId(String checkoutRequestId) {
    return jpaOrderRepository.findByMpesaCheckoutRequestId(checkoutRequestId).map(OrderEntity::toDomain);
  }

  @Override
  public Optional<Order> findByPublicIdForUser(PublicId orderPublicId, UserPublicId userPublicId) {
    return jpaOrderRepository.findByPublicIdAndUserPublicId(orderPublicId.value(), userPublicId.value())
      .map(OrderEntity::toDomain);
  }

  @Override
  public boolean updateStatusIfPending(PublicId orderPublicId, OrderStatus newStatus, String mpesaReceiptNumber) {
    return jpaOrderRepository.updateStatusIfCurrent(orderPublicId.value(), OrderStatus.PENDING,
      newStatus, mpesaReceiptNumber) == 1;
  }

  @Override
  public void attachMpesaReceipt(PublicId orderPublicId, String mpesaReceiptNumber) {
    jpaOrderRepository.updateStatusIfCurrent(orderPublicId.value(), OrderStatus.PAID,
      OrderStatus.PAID, mpesaReceiptNumber);
  }

  @Override
  public Page<Order> findAllByUserPublicId(UserPublicId userPublicId, Pageable pageable) {
    return jpaOrderRepository.findAllByUserPublicId(userPublicId.value(), pageable)
      .map(OrderEntity::toDomain);
  }

  @Override
  public Page<Order> findAll(Pageable pageable) {
    return jpaOrderRepository.findAll(pageable).map(OrderEntity::toDomain);
  }
}
