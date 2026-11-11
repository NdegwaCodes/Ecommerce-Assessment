package com.ndegwacodes.ecom.order.infrastructure.primary.order;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Address;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.ndegwacodes.ecom.order.application.OrderApplicationService;
import com.ndegwacodes.ecom.order.domain.order.CartPaymentException;
import com.ndegwacodes.ecom.order.domain.order.aggregate.*;
import com.ndegwacodes.ecom.order.domain.order.vo.StripeSessionId;
import com.ndegwacodes.ecom.order.infrastructure.secondary.service.mpesa.Daraja;
import com.ndegwacodes.ecom.order.infrastructure.secondary.service.mpesa.MpesaPaymentException;
import com.ndegwacodes.ecom.order.infrastructure.secondary.service.mpesa.MpesaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.ndegwacodes.ecom.order.domain.user.vo.*;
import com.ndegwacodes.ecom.product.domain.vo.PublicId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.ndegwacodes.ecom.product.infrastructure.primary.ProductsAdminResource.ROLE_ADMIN;

@RestController
@RequestMapping("/api/orders")
public class OrderResource {

  private final OrderApplicationService orderApplicationService;
  private final MpesaService mpesaService;

  @Value("${application.stripe.webhook-secret}")
  private String webhookSecret;

  public OrderResource(OrderApplicationService orderApplicationService, MpesaService mpesaService) {
    this.orderApplicationService = orderApplicationService;
    this.mpesaService = mpesaService;
  }

  @PostMapping("/mpesa")
  public ResponseEntity<?> payWithMpesa(@RequestBody RestMpesaCheckoutRequest request) {
    try {
      PublicId orderPublicId = orderApplicationService.startMpesaCheckout(
        RestCartItemRequest.to(request.items() == null ? List.of() : request.items()), request.phoneNumber());
      return ResponseEntity.status(201).body(Map.of("orderPublicId", orderPublicId.value()));
    } catch (MpesaPaymentException e) {
      return ResponseEntity.status(e.getReason().httpStatus()).body(Map.of("message", e.getMessage()));
    }
  }

  @GetMapping("/mpesa/{orderPublicId}")
  public ResponseEntity<?> getMpesaOrderStatus(@PathVariable UUID orderPublicId) {
    try {
      Order order = orderApplicationService.getMpesaOrder(new PublicId(orderPublicId));
      return ResponseEntity.ok(RestMpesaOrderStatus.from(order));
    } catch (MpesaPaymentException e) {
      return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
    }
  }

  @PostMapping("/mpesa/callback/{token}")
  public ResponseEntity<Map<String, Object>> mpesaCallback(@PathVariable String token,
                                                           @RequestBody JsonNode body) {
    if (!mpesaService.isValidCallbackToken(token)) {
      return ResponseEntity.notFound().build();
    }
    return Daraja.parseCallback(body)
      .map(result -> {
        orderApplicationService.handleMpesaResult(result);
        return ResponseEntity.ok(Map.<String, Object>of("ResultCode", 0, "ResultDesc", "Accepted"));
      })
      .orElseGet(() -> ResponseEntity.badRequest()
        .body(Map.of("ResultCode", 1, "ResultDesc", "Malformed callback")));
  }

  @GetMapping("/get-cart-details")
  public ResponseEntity<RestDetailCartResponse> getDetails(@RequestParam List<UUID> productIds) {
    List<DetailCartItemRequest> cartItemRequests = productIds.stream()
      .map(uuid -> new DetailCartItemRequest(new PublicId(uuid), 1))
      .toList();

    DetailCartRequest detailCartRequest = DetailCartRequestBuilder.detailCartRequest().items(cartItemRequests).build();
    DetailCartResponse cartDetails = orderApplicationService.getCartDetails(detailCartRequest);
    return ResponseEntity.ok(RestDetailCartResponse.from(cartDetails));
  }

  @PostMapping("/init-payment")
  public ResponseEntity<RestStripeSession> initPayment(@RequestBody List<RestCartItemRequest> items) {
    List<DetailCartItemRequest> detailCartItemRequests = RestCartItemRequest.to(items);
    try {
      StripeSessionId stripeSessionInformation = orderApplicationService.createOrder(detailCartItemRequests);
      RestStripeSession restStripeSession = RestStripeSession.from(stripeSessionInformation);
      return ResponseEntity.ok(restStripeSession);
    } catch (CartPaymentException cpe) {
      return ResponseEntity.badRequest().build();
    }
  }

  @PostMapping("/webhook")
  public ResponseEntity<Void> webhookStripe(@RequestBody String paymentEvent,
                                            @RequestHeader("Stripe-Signature") String stripeSignature) {
    Event event = null;
    try {
      event = Webhook.constructEvent(
        paymentEvent, stripeSignature, webhookSecret
      );
    } catch (SignatureVerificationException e) {
      return ResponseEntity.badRequest().build();
    }

    Optional<StripeObject> rawStripeObjectOpt = event.getDataObjectDeserializer().getObject();

    switch (event.getType()) {
      case "checkout.session.completed":
        handleCheckoutSessionCompleted(rawStripeObjectOpt.orElseThrow());
        break;
    }

    return ResponseEntity.ok().build();
  }

  private void handleCheckoutSessionCompleted(StripeObject rawStripeObject) {
    if (rawStripeObject instanceof Session session) {
      Address address = session.getCustomerDetails().getAddress();

      UserAddress userAddress = UserAddressBuilder.userAddress()
        .city(address.getCity())
        .country(address.getCountry())
        .zipCode(address.getPostalCode())
        .street(address.getLine1())
        .build();

      UserAddressToUpdate userAddressToUpdate = UserAddressToUpdateBuilder.userAddressToUpdate()
        .userAddress(userAddress)
        .userPublicId(new UserPublicId(UUID.fromString(session.getMetadata().get("user_public_id"))))
        .build();

      StripeSessionInformation sessionInformation = StripeSessionInformationBuilder.stripeSessionInformation()
        .userAddress(userAddressToUpdate)
        .stripeSessionId(new StripeSessionId(session.getId()))
        .build();

      orderApplicationService.updateOrder(sessionInformation);
    }
  }

  @GetMapping("/user")
  public ResponseEntity<Page<RestOrderRead>> getOrdersForConnectedUser(Pageable pageable) {
    Page<Order> orders = orderApplicationService.findOrdersForConnectedUser(pageable);
    PageImpl<RestOrderRead> restOrderReads = new PageImpl<>(
      orders.getContent().stream().map(RestOrderRead::from).toList(),
      pageable,
      orders.getTotalElements()
    );
    return ResponseEntity.ok(restOrderReads);
  }

  @GetMapping("/admin")
  @PreAuthorize("hasAnyRole('" + ROLE_ADMIN + "')")
  public ResponseEntity<Page<RestOrderReadAdmin>> getOrdersForAdmin(Pageable pageable) {
    Page<Order> orders = orderApplicationService.findOrdersForAdmin(pageable);
    PageImpl<RestOrderReadAdmin> restOrderReads = new PageImpl<>(
      orders.getContent().stream().map(RestOrderReadAdmin::from).toList(),
      pageable,
      orders.getTotalElements()
    );
    return ResponseEntity.ok(restOrderReads);
  }
}
