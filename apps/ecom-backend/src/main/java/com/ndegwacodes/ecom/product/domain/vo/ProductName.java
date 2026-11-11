package com.ndegwacodes.ecom.product.domain.vo;

import com.ndegwacodes.ecom.shared.error.domain.Assert;

public record ProductName(String value) {

  public ProductName {
    Assert.notNull("value", value);
    Assert.field("value", value).minLength(3).maxLength(256);
  }
}
