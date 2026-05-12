package org.example.milkteamanagement.dto.report;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record RevenueReportResponse(
        BigDecimal revenue,
        BigDecimal ingredientCost,
        BigDecimal profit,
        List<TopSellingItemResponse> topSelling,
        List<Map<String, Object>> timeline
) {
}


