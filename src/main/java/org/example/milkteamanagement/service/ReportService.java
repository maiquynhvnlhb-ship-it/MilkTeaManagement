package org.example.milkteamanagement.service;

import org.example.milkteamanagement.dto.report.ReportGroupBy;
import org.example.milkteamanagement.dto.report.RevenueReportResponse;

import java.time.LocalDateTime;

public interface ReportService {
    RevenueReportResponse revenue(LocalDateTime from, LocalDateTime to, ReportGroupBy groupBy);
}


