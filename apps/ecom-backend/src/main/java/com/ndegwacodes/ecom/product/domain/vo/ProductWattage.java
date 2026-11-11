package com.ndegwacodes.ecom.product.domain.vo;

import com.ndegwacodes.ecom.shared.error.domain.Assert;

public record ProductWattage(int value) {

  public ProductWattage {
    Assert.field("wattage", value).min(1).max(100_000);
  }
}
