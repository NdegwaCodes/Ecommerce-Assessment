package com.ndegwacodes.ecom.order.domain.order.vo;

import com.ndegwacodes.ecom.shared.error.domain.Assert;

public record StripeSessionId(String value) {

  public StripeSessionId {
    Assert.notNull("value", value);
  }
}
