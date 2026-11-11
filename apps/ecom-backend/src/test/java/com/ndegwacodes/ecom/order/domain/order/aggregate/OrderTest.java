package com.ndegwacodes.ecom.order.domain.order.aggregate;

import com.ndegwacodes.ecom.order.domain.order.vo.OrderPrice;
import com.ndegwacodes.ecom.order.domain.order.vo.OrderQuantity;
import com.ndegwacodes.ecom.order.domain.order.vo.OrderStatus;
import com.ndegwacodes.ecom.order.domain.order.vo.PaymentMethod;
import com.ndegwacodes.ecom.order.domain.order.vo.ProductPublicId;
import com.ndegwacodes.ecom.product.domain.vo.ProductName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

  private static OrderedProduct line(double price, long quantity) {
    return new OrderedProduct(new ProductPublicId(UUID.randomUUID()), new OrderPrice(price),
      new OrderQuantity(quantity), new ProductName("Solar panel"));
  }

  @Test
  void totalIsPriceTimesQuantityRoundedUpToWholeShillings() {
    assertThat(Order.totalInShillings(List.of(line(12500, 2), line(4999.5, 1)))).isEqualTo(30000L);
  }

  @Test
  void mpesaOrdersStartPendingWithTheirCheckoutRequest() {
    Order order = Order.createForMpesa(null, List.of(line(8000, 1)), "ws_CO_123", "254712345678");
    assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    assertThat(order.isPending()).isTrue();
    assertThat(order.getPaymentMethod()).isEqualTo(PaymentMethod.MPESA);
    assertThat(order.getMpesaCheckoutRequestId()).isEqualTo("ws_CO_123");
    assertThat(order.getStripeId()).isNull();
    assertThat(order.totalInShillings()).isEqualTo(8000L);
  }
}
