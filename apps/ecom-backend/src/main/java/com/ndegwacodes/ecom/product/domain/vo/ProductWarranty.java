package com.ndegwacodes.ecom.product.domain.vo;

import com.ndegwacodes.ecom.shared.error.domain.Assert;

public record ProductWarranty(int years) {

  public ProductWarranty {
    Assert.field("warrantyYears", years).min(0).max(30);
  }
}
