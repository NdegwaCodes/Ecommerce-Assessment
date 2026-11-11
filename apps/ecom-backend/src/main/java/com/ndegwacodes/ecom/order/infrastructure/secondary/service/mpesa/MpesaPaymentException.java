package com.ndegwacodes.ecom.order.infrastructure.secondary.service.mpesa;

public class MpesaPaymentException extends RuntimeException {

  public enum Reason {
    INVALID_REQUEST(400),
    OUT_OF_STOCK(409),
    REJECTED(502),
    NOT_CONFIGURED(503);

    private final int httpStatus;

    Reason(int httpStatus) {
      this.httpStatus = httpStatus;
    }

    public int httpStatus() {
      return httpStatus;
    }
  }

  private final Reason reason;

  public MpesaPaymentException(Reason reason, String message) {
    super(message);
    this.reason = reason;
  }

  public Reason getReason() {
    return reason;
  }
}
