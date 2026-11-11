package com.ndegwacodes.ecom.order.domain.order.vo;

import com.ndegwacodes.ecom.shared.error.domain.Assert;

import java.util.UUID;

public record ProductPublicId(UUID value) {

  public ProductPublicId {
    Assert.notNull("value", value);
  }
}
