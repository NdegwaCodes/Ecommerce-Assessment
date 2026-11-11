package com.ndegwacodes.ecom.product.domain.aggregate;

import com.ndegwacodes.ecom.product.domain.vo.PublicId;
import org.jilt.Builder;

@Builder
public record FilterQuery(PublicId categoryId, Integer minWattage, Integer maxWattage) {
}
