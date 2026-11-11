package com.ndegwacodes.ecom.order.infrastructure.secondary.entity;

import com.ndegwacodes.ecom.order.domain.order.aggregate.Order;
import com.ndegwacodes.ecom.order.domain.order.aggregate.OrderBuilder;
import com.ndegwacodes.ecom.order.domain.order.aggregate.OrderedProduct;
import com.ndegwacodes.ecom.order.domain.order.vo.OrderStatus;
import com.ndegwacodes.ecom.order.domain.order.vo.PaymentMethod;
import com.ndegwacodes.ecom.product.domain.vo.PublicId;
import com.ndegwacodes.ecom.shared.jpa.AbstractAuditingEntity;
import jakarta.persistence.*;
import org.jilt.Builder;

import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "order")
@Builder
public class OrderEntity extends AbstractAuditingEntity<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orderSequenceGenerator")
  @SequenceGenerator(name = "orderSequenceGenerator", sequenceName = "order_sequence", allocationSize = 1)
  @Column(name = "id")
  private Long id;

  @Column(name = "public_id", nullable = false, unique = true)
  private UUID publicId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OrderStatus status;

  @Column(name = "stripe_session_id")
  private String stripeSessionId;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method", nullable = false)
  private PaymentMethod paymentMethod;

  @Column(name = "mpesa_checkout_request_id", unique = true)
  private String mpesaCheckoutRequestId;

  @Column(name = "mpesa_receipt_number")
  private String mpesaReceiptNumber;

  @Column(name = "mpesa_phone_number")
  private String mpesaPhoneNumber;

  @OneToMany(mappedBy = "id.order", cascade = CascadeType.REMOVE)
  private Set<OrderedProductEntity> orderedProducts = new HashSet<>();

  @ManyToOne
  @JoinColumn(name = "fk_customer", nullable = false)
  private UserEntity user;

  public OrderEntity() {
  }

  public OrderEntity(Long id, UUID publicId, OrderStatus status, String stripeSessionId,
                     PaymentMethod paymentMethod, String mpesaCheckoutRequestId,
                     String mpesaReceiptNumber, String mpesaPhoneNumber,
                     Set<OrderedProductEntity> orderedProducts, UserEntity user) {
    this.id = id;
    this.publicId = publicId;
    this.status = status;
    this.stripeSessionId = stripeSessionId;
    this.paymentMethod = paymentMethod;
    this.mpesaCheckoutRequestId = mpesaCheckoutRequestId;
    this.mpesaReceiptNumber = mpesaReceiptNumber;
    this.mpesaPhoneNumber = mpesaPhoneNumber;
    this.orderedProducts = orderedProducts;
    this.user = user;
  }

  public static OrderEntity from(Order order) {
    Set<OrderedProductEntity> orderedProductEntities = order.getOrderedProducts()
      .stream().map(OrderedProductEntity::from).collect(Collectors.toSet());

    return OrderEntityBuilder.orderEntity()
      .publicId(order.getPublicId().value())
      .status(order.getStatus())
      .stripeSessionId(order.getStripeId())
      .paymentMethod(order.getPaymentMethod())
      .mpesaCheckoutRequestId(order.getMpesaCheckoutRequestId())
      .mpesaReceiptNumber(order.getMpesaReceiptNumber())
      .mpesaPhoneNumber(order.getMpesaPhoneNumber())
      .orderedProducts(orderedProductEntities)
      .user(UserEntity.from(order.getUser()))
      .build();
  }

  public static Order toDomain(OrderEntity orderEntity) {
    Set<OrderedProduct> orderedProducts = orderEntity.getOrderedProducts().stream()
      .map(OrderedProductEntity::toDomain).collect(Collectors.toSet());

    return OrderBuilder.order()
      .publicId(new PublicId(orderEntity.getPublicId()))
      .status(orderEntity.getStatus())
      .stripeId(orderEntity.getStripeSessionId())
      .paymentMethod(orderEntity.getPaymentMethod())
      .mpesaCheckoutRequestId(orderEntity.getMpesaCheckoutRequestId())
      .mpesaReceiptNumber(orderEntity.getMpesaReceiptNumber())
      .mpesaPhoneNumber(orderEntity.getMpesaPhoneNumber())
      .user(UserEntity.toDomain(orderEntity.getUser()))
      .orderedProducts(orderedProducts.stream().toList())
      .build();
  }

  @Override
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public UUID getPublicId() {
    return publicId;
  }

  public void setPublicId(UUID publicId) {
    this.publicId = publicId;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public void setStatus(OrderStatus status) {
    this.status = status;
  }

  public String getStripeSessionId() {
    return stripeSessionId;
  }

  public void setStripeSessionId(String stripeSessionId) {
    this.stripeSessionId = stripeSessionId;
  }

  public PaymentMethod getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(PaymentMethod paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public String getMpesaCheckoutRequestId() {
    return mpesaCheckoutRequestId;
  }

  public void setMpesaCheckoutRequestId(String mpesaCheckoutRequestId) {
    this.mpesaCheckoutRequestId = mpesaCheckoutRequestId;
  }

  public String getMpesaReceiptNumber() {
    return mpesaReceiptNumber;
  }

  public void setMpesaReceiptNumber(String mpesaReceiptNumber) {
    this.mpesaReceiptNumber = mpesaReceiptNumber;
  }

  public String getMpesaPhoneNumber() {
    return mpesaPhoneNumber;
  }

  public void setMpesaPhoneNumber(String mpesaPhoneNumber) {
    this.mpesaPhoneNumber = mpesaPhoneNumber;
  }

  public Set<OrderedProductEntity> getOrderedProducts() {
    return orderedProducts;
  }

  public void setOrderedProducts(Set<OrderedProductEntity> orderedProducts) {
    this.orderedProducts = orderedProducts;
  }

  public UserEntity getUser() {
    return user;
  }

  public void setUser(UserEntity user) {
    this.user = user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OrderEntity order)) return false;
    return Objects.equals(publicId, order.publicId) && status == order.status && Objects.equals(stripeSessionId, order.stripeSessionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(publicId, status, stripeSessionId);
  }
}
