package com.abhay.management.site.controller;

import com.abhay.management.site.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    // ── GET /api/reports/excel — download full work report ────────────────────
    @GetMapping("/excel")
    public ResponseEntity<byte[]> downloadExcelReport() {
        try {
            byte[] data = reportService.generateAllWorkReport();

            String filename = "sitetracker_report_" +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
                    + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .contentLength(data.length)
                    .body(data);

        } catch (IOException e) {
            log.error("Failed to generate report", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
