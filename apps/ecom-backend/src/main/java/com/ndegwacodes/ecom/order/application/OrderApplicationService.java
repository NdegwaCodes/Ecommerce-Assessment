package com.ndegwacodes.ecom.order.application;

import com.ndegwacodes.ecom.order.domain.order.aggregate.*;
import com.ndegwacodes.ecom.order.domain.order.repository.OrderRepository;
import com.ndegwacodes.ecom.order.domain.order.service.CartReader;
import com.ndegwacodes.ecom.order.domain.order.service.OrderCreator;
import com.ndegwacodes.ecom.order.domain.order.service.OrderReader;
import com.ndegwacodes.ecom.order.domain.order.service.OrderUpdater;
import com.ndegwacodes.ecom.order.domain.order.vo.OrderStatus;
import com.ndegwacodes.ecom.order.domain.order.vo.StripeSessionId;
import com.ndegwacodes.ecom.order.domain.user.aggregate.User;
import com.ndegwacodes.ecom.order.infrastructure.secondary.service.mpesa.Daraja;
import com.ndegwacodes.ecom.order.infrastructure.secondary.service.mpesa.MpesaCallbackResult;
import com.ndegwacodes.ecom.order.infrastructure.secondary.service.mpesa.MpesaPaymentException;
import com.ndegwacodes.ecom.order.infrastructure.secondary.service.mpesa.MpesaService;
import com.ndegwacodes.ecom.order.infrastructure.secondary.service.mpesa.MpesaStkPush;
import com.ndegwacodes.ecom.order.infrastructure.secondary.service.stripe.StripeService;
import com.ndegwacodes.ecom.product.application.ProductsApplicationService;
import com.ndegwacodes.ecom.product.domain.aggregate.Product;
import com.ndegwacodes.ecom.product.domain.vo.PublicId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderApplicationService {

  private static final Logger log = LoggerFactory.getLogger(OrderApplicationService.class);

  private final ProductsApplicationService productsApplicationService;
  private final CartReader cartReader;
  private final UsersApplicationService usersApplicationService;
  private final OrderCreator orderCreator;
  private final OrderUpdater orderUpdater;
  private final OrderReader orderReader;
  private final OrderRepository orderRepository;
  private final MpesaService mpesaService;

  public OrderApplicationService(ProductsApplicationService productsApplicationService,
                                 UsersApplicationService usersApplicationService,
                                 OrderRepository orderRepository,
                                 StripeService stripeService,
                                 MpesaService mpesaService) {
    this.productsApplicationService = productsApplicationService;
    this.usersApplicationService = usersApplicationService;
    this.cartReader = new CartReader();
    this.orderCreator = new OrderCreator(orderRepository, stripeService);
    this.orderUpdater = new OrderUpdater(orderRepository);
    this.orderReader = new OrderReader(orderRepository);
    this.orderRepository = orderRepository;
    this.mpesaService = mpesaService;
  }

  @Transactional(readOnly = true)
  public DetailCartResponse getCartDetails(DetailCartRequest detailCartRequest) {
    List<PublicId> publicIds = detailCartRequest.items().stream().map(DetailCartItemRequest::productId).toList();
    List<Product> productsInformation = productsApplicationService.getProductsByPublicIdsIn(publicIds);
    return cartReader.getDetails(productsInformation);
  }

  @Transactional
  public StripeSessionId createOrder(List<DetailCartItemRequest> items) {
    User authenticatedUser = usersApplicationService.getAuthenticatedUser();
    List<PublicId> publicIds = items.stream().map(DetailCartItemRequest::productId).toList();
    List<Product> productsInformation = productsApplicationService.getProductsByPublicIdsIn(publicIds);
    return orderCreator.create(productsInformation, items, authenticatedUser);
  }

  @Transactional
  public void updateOrder(StripeSessionInformation stripeSessionInformation) {
    List<OrderedProduct> orderedProducts = this.orderUpdater.updateOrderFromStripe(stripeSessionInformation);
    List<OrderProductQuantity> orderProductQuantities = this.orderUpdater.computeQuantity(orderedProducts);
    this.productsApplicationService.updateProductQuantity(orderProductQuantities);
    this.usersApplicationService.updateAddress(stripeSessionInformation.userAddress());
  }

  @Transactional
  public PublicId startMpesaCheckout(List<DetailCartItemRequest> items, String phoneNumber) {
    if (items == null || items.isEmpty() || items.stream().anyMatch(item -> item.quantity() < 1)) {
      throw new MpesaPaymentException(MpesaPaymentException.Reason.INVALID_REQUEST, "Your cart is empty");
    }
    String phone = Daraja.normalisePhone(phoneNumber)
      .orElseThrow(() -> new MpesaPaymentException(MpesaPaymentException.Reason.INVALID_REQUEST,
        "Enter a Safaricom number like 0712 345 678"));

    User authenticatedUser = usersApplicationService.getAuthenticatedUser();
    List<PublicId> publicIds = items.stream().map(DetailCartItemRequest::productId).toList();
    List<Product> products = productsApplicationService.getProductsByPublicIdsIn(publicIds);

    List<OrderedProduct> orderedProducts = new ArrayList<>();
    for (DetailCartItemRequest item : items) {
      Product product = products.stream()
        .filter(candidate -> candidate.getPublicId().value().equals(item.productId().value()))
        .findFirst()
        .orElseThrow(() -> new MpesaPaymentException(MpesaPaymentException.Reason.INVALID_REQUEST,
          "A product in your cart is no longer available"));
      if (product.getNbInStock() < item.quantity()) {
        throw new MpesaPaymentException(MpesaPaymentException.Reason.OUT_OF_STOCK,
          "Only " + product.getNbInStock() + " left of " + product.getName().value());
      }
      orderedProducts.add(OrderedProduct.create(item.quantity(), product));
    }

    long amount = Order.totalInShillings(orderedProducts);
    MpesaStkPush push = mpesaService.stkPush(phone, amount, "Solar order");
    Order order = Order.createForMpesa(authenticatedUser, orderedProducts, push.checkoutRequestId(), phone);
    orderRepository.save(order);
    return order.getPublicId();
  }

  @Transactional
  public void handleMpesaResult(MpesaCallbackResult result) {
    Optional<Order> orderOpt = orderRepository.findByMpesaCheckoutRequestId(result.checkoutRequestId());
    if (orderOpt.isEmpty()) {
      log.warn("M-Pesa result for unknown checkout request {}", result.checkoutRequestId());
      return;
    }
    Order order = orderOpt.get();
    if (!order.isPending()) {
      if (order.getStatus() == OrderStatus.PAID && order.getMpesaReceiptNumber() == null
        && result.succeeded() && result.receiptNumber() != null) {
        orderRepository.attachMpesaReceipt(order.getPublicId(), result.receiptNumber());
      }
      return;
    }

    boolean paid = result.succeeded();
    if (paid && result.amount() != null && result.amount() != order.totalInShillings()) {
      log.error("M-Pesa amount mismatch on order {}: expected {}, received {}",
        order.getPublicId().value(), order.totalInShillings(), result.amount());
      paid = false;
    }

    boolean updated = orderRepository.updateStatusIfPending(order.getPublicId(),
      paid ? OrderStatus.PAID : OrderStatus.FAILED, paid ? result.receiptNumber() : null);
    if (updated && paid) {
      productsApplicationService.updateProductQuantity(orderUpdater.computeQuantity(order.getOrderedProducts()));
    }
  }

  @Transactional
  public Order getMpesaOrder(PublicId orderPublicId) {
    User authenticatedUser = usersApplicationService.getAuthenticatedUser();
    Order order = orderRepository.findByPublicIdForUser(orderPublicId, authenticatedUser.getUserPublicId())
      .orElseThrow(() -> new MpesaPaymentException(MpesaPaymentException.Reason.INVALID_REQUEST, "Order not found"));
    if (order.isPending() && order.getMpesaCheckoutRequestId() != null) {
      Optional<Integer> resultCode = mpesaService.stkQuery(order.getMpesaCheckoutRequestId());
      if (resultCode.isPresent()) {
        handleMpesaResult(new MpesaCallbackResult(order.getMpesaCheckoutRequestId(), resultCode.get(), "", null, null));
        order = orderRepository.findByPublicIdForUser(orderPublicId, authenticatedUser.getUserPublicId()).orElseThrow();
      }
    }
    return order;
  }

  @Transactional(readOnly = true)
  public Page<Order> findOrdersForConnectedUser(Pageable pageable) {
    User authenticatedUser = usersApplicationService.getAuthenticatedUser();
    return orderReader.findAllByUserPublicId(authenticatedUser.getUserPublicId(), pageable);
  }

  @Transactional(readOnly = true)
  public Page<Order> findOrdersForAdmin(Pageable pageable) {
    return orderReader.findAll(pageable);
  }
}
