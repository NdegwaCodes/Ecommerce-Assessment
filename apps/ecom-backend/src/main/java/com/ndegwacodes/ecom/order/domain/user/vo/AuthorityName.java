package com.ndegwacodes.ecom.order.domain.user.vo;

import com.ndegwacodes.ecom.shared.error.domain.Assert;

public record AuthorityName(String name) {

  public AuthorityName {
    Assert.field("name", name).notNull();
  }
}
