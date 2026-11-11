package com.ndegwacodes.ecom.order.infrastructure.primary.order;

import com.ndegwacodes.ecom.order.domain.order.aggregate.Order;
import com.ndegwacodes.ecom.order.domain.order.vo.OrderStatus;

import java.util.UUID;

public record RestMpesaOrderStatus(UUID orderPublicId, OrderStatus status, String receiptNumber, long amount) {

  public static RestMpesaOrderStatus from(Order order) {
    return new RestMpesaOrderStatus(order.getPublicId().value(), order.getStatus(),
      order.getMpesaReceiptNumber(), order.totalInShillings());
  }
}
