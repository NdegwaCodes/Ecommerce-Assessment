package com.ndegwacodes.ecom.order.domain.order.service;

import com.ndegwacodes.ecom.order.domain.order.aggregate.*;
import com.ndegwacodes.ecom.order.domain.order.repository.OrderRepository;
import com.ndegwacodes.ecom.order.domain.order.vo.OrderStatus;

import java.util.ArrayList;
import java.util.List;

public class OrderUpdater {

  private final OrderRepository orderRepository;

  public OrderUpdater(OrderRepository orderRepository) {
    this.orderRepository = orderRepository;
  }

  public List<OrderedProduct> updateOrderFromStripe(StripeSessionInformation stripeSessionInformation) {
    Order order = orderRepository.findByStripeSessionId(stripeSessionInformation).orElseThrow();
    if (!order.isPending()) {
      return List.of();
    }
    boolean updated = orderRepository.updateStatusIfPending(order.getPublicId(), OrderStatus.PAID, null);
    return updated ? order.getOrderedProducts() : List.of();
  }

  public List<OrderProductQuantity> computeQuantity(List<OrderedProduct> orderedProducts) {
    List<OrderProductQuantity> orderProductQuantities = new ArrayList<>();
    for (OrderedProduct orderedProduct : orderedProducts) {
      OrderProductQuantity orderProductQuantity = OrderProductQuantityBuilder.orderProductQuantity()
        .productPublicId(orderedProduct.getProductPublicId())
        .quantity(orderedProduct.getQuantity())
        .build();
      orderProductQuantities.add(orderProductQuantity);
    }
    return orderProductQuantities;
  }
}
