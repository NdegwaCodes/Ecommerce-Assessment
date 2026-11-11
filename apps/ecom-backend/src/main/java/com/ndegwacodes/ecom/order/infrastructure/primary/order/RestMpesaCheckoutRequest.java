package com.ndegwacodes.ecom.order.infrastructure.primary.order;

import java.util.List;

public record RestMpesaCheckoutRequest(List<RestCartItemRequest> items, String phoneNumber) {
}
