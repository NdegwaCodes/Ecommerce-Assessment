package com.ndegwacodes.ecom.order.domain.order.aggregate;

import com.ndegwacodes.ecom.order.domain.order.vo.OrderStatus;
import com.ndegwacodes.ecom.order.domain.order.vo.PaymentMethod;
import com.ndegwacodes.ecom.order.domain.order.vo.StripeSessionId;
import com.ndegwacodes.ecom.order.domain.user.aggregate.User;
import com.ndegwacodes.ecom.product.domain.vo.PublicId;
import org.jilt.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public class Order {

  private OrderStatus status;
  private User user;
  private String stripeId;
  private PublicId publicId;
  private List<OrderedProduct> orderedProducts;
  private PaymentMethod paymentMethod;
  private String mpesaCheckoutRequestId;
  private String mpesaReceiptNumber;
  private String mpesaPhoneNumber;

  public Order(OrderStatus status, User user, String stripeId, PublicId publicId,
               List<OrderedProduct> orderedProducts, PaymentMethod paymentMethod,
               String mpesaCheckoutRequestId, String mpesaReceiptNumber, String mpesaPhoneNumber) {
    this.status = status;
    this.user = user;
    this.stripeId = stripeId;
    this.publicId = publicId;
    this.orderedProducts = orderedProducts;
    this.paymentMethod = paymentMethod == null ? PaymentMethod.STRIPE : paymentMethod;
    this.mpesaCheckoutRequestId = mpesaCheckoutRequestId;
    this.mpesaReceiptNumber = mpesaReceiptNumber;
    this.mpesaPhoneNumber = mpesaPhoneNumber;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public User getUser() {
    return user;
  }

  public String getStripeId() {
    return stripeId;
  }

  public PublicId getPublicId() {
    return publicId;
  }

  public List<OrderedProduct> getOrderedProducts() {
    return orderedProducts;
  }

  public PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  public String getMpesaCheckoutRequestId() {
    return mpesaCheckoutRequestId;
  }

  public String getMpesaReceiptNumber() {
    return mpesaReceiptNumber;
  }

  public String getMpesaPhoneNumber() {
    return mpesaPhoneNumber;
  }

  public static Order create(User connectedUser, List<OrderedProduct> orderedProducts,
                             StripeSessionId stripeSessionId) {
    return OrderBuilder.order()
      .publicId(new PublicId(UUID.randomUUID()))
      .user(connectedUser)
      .status(OrderStatus.PENDING)
      .orderedProducts(orderedProducts)
      .stripeId(stripeSessionId.value())
      .paymentMethod(PaymentMethod.STRIPE)
      .build();
  }

  public static Order createForMpesa(User connectedUser, List<OrderedProduct> orderedProducts,
                                     String checkoutRequestId, String phoneNumber) {
    return OrderBuilder.order()
      .publicId(new PublicId(UUID.randomUUID()))
      .user(connectedUser)
      .status(OrderStatus.PENDING)
      .orderedProducts(orderedProducts)
      .paymentMethod(PaymentMethod.MPESA)
      .mpesaCheckoutRequestId(checkoutRequestId)
      .mpesaPhoneNumber(phoneNumber)
      .build();
  }

  public static long totalInShillings(List<OrderedProduct> orderedProducts) {
    double total = orderedProducts.stream()
      .mapToDouble(product -> product.getPrice().value() * product.getQuantity().value())
      .sum();
    return (long) Math.ceil(total);
  }

  public long totalInShillings() {
    return totalInShillings(orderedProducts);
  }

  public boolean isPending() {
    return status == OrderStatus.PENDING;
  }

  public void validatePayment() {
    this.status = OrderStatus.PAID;
  }
}
