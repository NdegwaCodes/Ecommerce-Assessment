package com.ndegwacodes.ecom.order.domain.order.aggregate;

import com.ndegwacodes.ecom.product.domain.vo.PublicId;
import org.jilt.Builder;

@Builder
public record DetailCartItemRequest(PublicId productId, long quantity) {
}
