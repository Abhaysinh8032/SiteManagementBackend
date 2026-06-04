package com.abhay.management.site.service;

import com.abhay.management.site.entity.Task;
import com.abhay.management.site.entity.WorkUpdate;
import com.abhay.management.site.repository.TaskRepository;
import com.abhay.management.site.repository.WorkUpdateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final TaskRepository       taskRepository;
    private final WorkUpdateRepository workUpdateRepository;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // ── Generate all-work Excel report ────────────────────────────────────────

    public byte[] generateAllWorkReport() throws IOException {
        List<Task> tasks = taskRepository.findAll();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            // ── Styles ────────────────────────────────────────────────────────
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle altStyle = workbook.createCellStyle();
            altStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
            altStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setColor(IndexedColors.DARK_RED.getIndex());
            titleStyle.setFont(titleFont);

            // ── Sheet 1: Tasks overview ───────────────────────────────────────
            Sheet taskSheet = workbook.createSheet("All Tasks");

            // Title row
            Row titleRow = taskSheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("SiteTracker — Work Report");
            titleCell.setCellStyle(titleStyle);
            taskSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

            // Generated date row
            Row genRow = taskSheet.createRow(1);
            genRow.createCell(0).setCellValue(
                    "Generated: " + java.time.LocalDateTime.now().format(FMT));

            // Header row
            String[] taskHeaders = {
                    "Site Name", "Site Location", "Task Title", "Description",
                    "Assigned To", "Employee ID", "Status", "Created At"
            };
            Row headerRow = taskSheet.createRow(3);
            for (int i = 0; i < taskHeaders.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(taskHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowNum = 4;
            for (Task task : tasks) {
                Row row = taskSheet.createRow(rowNum++);
                if (rowNum % 2 == 0) {
                    for (int c = 0; c < taskHeaders.length; c++) {
                        row.createCell(c).setCellStyle(altStyle);
                    }
                }
                row.createCell(0).setCellValue(task.getSite().getName());
                row.createCell(1).setCellValue(task.getSite().getLocation());
                row.createCell(2).setCellValue(task.getTitle());
                row.createCell(3).setCellValue(task.getDescription() != null
                        ? task.getDescription() : "");
                row.createCell(4).setCellValue(task.getAssignedTo().getName());
                row.createCell(5).setCellValue(task.getAssignedTo().getEmployeeId());
                row.createCell(6).setCellValue(task.getStatus().name());
                row.createCell(7).setCellValue(task.getCreatedAt() != null
                        ? task.getCreatedAt().format(FMT) : "");
            }

            // Auto-size columns
            for (int i = 0; i < taskHeaders.length; i++) {
                taskSheet.autoSizeColumn(i);
            }

            // ── Sheet 2: Work updates ─────────────────────────────────────────
            Sheet updateSheet = workbook.createSheet("Work Updates");

            Row uTitleRow = updateSheet.createRow(0);
            Cell uTitleCell = uTitleRow.createCell(0);
            uTitleCell.setCellValue("SiteTracker — Work Updates");
            uTitleCell.setCellStyle(titleStyle);
            updateSheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            String[] updateHeaders = {
                    "Site", "Task Title", "Worker Name", "Employee ID",
                    "Update Text", "Status At Update", "Date"
            };
            Row uHeaderRow = updateSheet.createRow(2);
            for (int i = 0; i < updateHeaders.length; i++) {
                Cell cell = uHeaderRow.createCell(i);
                cell.setCellValue(updateHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            List<WorkUpdate> updates = workUpdateRepository.findAll();
            int uRowNum = 3;
            for (WorkUpdate update : updates) {
                Row row = updateSheet.createRow(uRowNum++);
                row.createCell(0).setCellValue(update.getTask().getSite().getName());
                row.createCell(1).setCellValue(update.getTask().getTitle());
                row.createCell(2).setCellValue(update.getUser().getName());
                row.createCell(3).setCellValue(update.getUser().getEmployeeId());
                row.createCell(4).setCellValue(update.getUpdateText());
                row.createCell(5).setCellValue(update.getStatusAtUpdate() != null
                        ? update.getStatusAtUpdate() : "");
                row.createCell(6).setCellValue(update.getCreatedAt() != null
                        ? update.getCreatedAt().format(FMT) : "");
            }

            for (int i = 0; i < updateHeaders.length; i++) {
                updateSheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            log.info("Excel report generated: {} tasks, {} updates", tasks.size(), updates.size());
            return out.toByteArray();
        }
    }
}
