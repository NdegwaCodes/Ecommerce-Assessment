package com.ndegwacodes.ecom.order.domain.order.vo;

import com.ndegwacodes.ecom.shared.error.domain.Assert;

public record OrderQuantity(long value) {

  public OrderQuantity {
    Assert.field("value", value).positive();

  }
}
