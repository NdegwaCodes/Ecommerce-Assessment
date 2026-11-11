package com.ndegwacodes.ecom.order.domain.user.vo;

import com.ndegwacodes.ecom.shared.error.domain.Assert;

import java.util.UUID;

public record UserPublicId(UUID value) {

  public UserPublicId {
    Assert.notNull("value", value);
  }
}
