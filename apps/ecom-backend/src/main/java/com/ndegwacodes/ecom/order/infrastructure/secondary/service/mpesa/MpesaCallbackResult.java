package com.ndegwacodes.ecom.order.infrastructure.secondary.service.mpesa;

public record MpesaCallbackResult(String checkoutRequestId, int resultCode, String resultDesc,
                                  Long amount, String receiptNumber) {

  public boolean succeeded() {
    return resultCode == 0;
  }
}
