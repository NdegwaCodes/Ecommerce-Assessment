package com.ndegwacodes.ecom.order.domain.order.vo;

import com.ndegwacodes.ecom.shared.error.domain.Assert;

public record OrderPrice(double value) {

  public OrderPrice {
    Assert.field("value", value).strictlyPositive();
  }
}
