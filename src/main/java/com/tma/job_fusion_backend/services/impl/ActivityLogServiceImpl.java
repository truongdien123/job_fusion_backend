package com.tma.job_fusion_backend.services.impl;

import com.tma.job_fusion_backend.commons.ErrorCode;
import com.tma.job_fusion_backend.commons.RoleConstant;
import com.tma.job_fusion_backend.components.UserPrincipal;
import com.tma.job_fusion_backend.mappers.ActivityLogMapper;
import com.tma.job_fusion_backend.models.ActivityLog;
import com.tma.job_fusion_backend.pojo.dtos.ActivityLogFilter;
import com.tma.job_fusion_backend.pojo.requests.PagingRequest;
import com.tma.job_fusion_backend.pojo.responses.ActivityLogResponse;
import com.tma.job_fusion_backend.repositories.query.ActivityLogQueryRepository;
import com.tma.job_fusion_backend.repositories.ActivityLogRepository;
import com.tma.job_fusion_backend.repositories.UserRepository;
import com.tma.job_fusion_backend.services.ActivityLogService;
import com.tma.job_fusion_backend.utils.ValidationUtil;
import com.tma.job_fusion_backend.enums.EventType;
import com.tma.job_fusion_backend.enums.JobPostingAction;
import com.tma.job_fusion_backend.models.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import org.springframework.transaction.annotation.Transactional;
import com.tma.job_fusion_backend.exceptions.NotFoundException;
import com.tma.job_fusion_backend.utils.DateTimeUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private static final String EXCEL_FONT_NAME = "Calibri";
    private static final String EXCEL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final ActivityLogQueryRepository activityLogQueryRepository;
    private final ActivityLogMapper activityLogMapper;
    private final ValidationUtil validationUtil;
    private final ActivityLogRepository activityLogRepository;
    private final UserRepository userRepository;
    private final HttpServletRequest request;

    @Override
    public Page<ActivityLogResponse> getListActivityLog(PagingRequest<ActivityLogFilter> request) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();

        if (!currentUser.hasRole(RoleConstant.TENANT_ADMIN)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        UUID tenantId = currentUser.getTenantId();
        if (ObjectUtils.isEmpty(tenantId)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        ActivityLogFilter filter = (ObjectUtils.isNotEmpty(request) && ObjectUtils.isNotEmpty(request.getFilters()))
                ? request.getFilters()
                : new ActivityLogFilter();

        Pageable pageable = request.toPageable();
        Page<ActivityLog> logPage = activityLogQueryRepository.findAllActivityLogs(filter, pageable);

        return logPage.map(activityLogMapper::toResponse);
    }

    @Override
    @Transactional
    public void log(UUID userId, EventType eventType, String description) {
        log(userId, eventType, description, null, null);
    }

    @Override
    @Transactional
    public void log(UUID userId, EventType eventType, String description, UUID jobPostingId, JobPostingAction action) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId).orElse(null);
        if (ObjectUtils.isEmpty(user)) {
            return;
        }

        ActivityLog log = new ActivityLog();
        log.setUser(user);
        log.setEventType(eventType);
        log.setDescription(description);
        log.setIpAddress(getClientIp());
        log.setCreatedBy(userId);
        log.setJobPostingId(jobPostingId);
        log.setAction(action);

        activityLogRepository.save(log);
    }

    private String getClientIp() {
        String[] IP_HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "X-Real-IP"
        };

        for (String header : IP_HEADERS) {
            String ip = request.getHeader(header);
            if (StringUtils.isNotEmpty(ip) && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    return ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    private User validateAndGetStaff(UUID staffId) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();

        if (!currentUser.hasRole(RoleConstant.TENANT_ADMIN)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        UUID tenantId = currentUser.getTenantId();
        if (ObjectUtils.isEmpty(tenantId)) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        User staff = userRepository.findByIdAndDeletedAtIsNull(staffId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        if (ObjectUtils.isEmpty(staff.getTenant()) || !tenantId.equals(staff.getTenant().getId())) {
            throw new AccessDeniedException(ErrorCode.ACCESS_DENIED);
        }

        return staff;
    }

    private Font createFont(Workbook workbook, String fontName, short size, boolean bold, boolean italic) {
        Font font = workbook.createFont();
        font.setFontName(fontName);
        font.setFontHeightInPoints(size);
        font.setBold(bold);
        font.setItalic(italic);
        return font;
    }

    @Override
    @Transactional
    public void deleteAllActivityLog(UUID staffId) {
        UserPrincipal currentUser = validationUtil.getRequiredCurrentUser();
        validateAndGetStaff(staffId);
        LocalDateTime now = DateTimeUtil.nowUtc();
        activityLogQueryRepository.softDeleteAllByUserId(staffId, now, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportActivityLogToExcel(UUID staffId) {
        validateAndGetStaff(staffId);
        List<ActivityLog> logs = activityLogRepository.findAllByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(staffId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Activity Logs");

            // Define styles using helper method and constants
            Font headerFont = createFont(workbook, EXCEL_FONT_NAME, (short) 11, true, false);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(headerStyle);

            Font dataFont = createFont(workbook, EXCEL_FONT_NAME, (short) 11, false, false);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setFont(dataFont);
            setBorder(dataStyle);

            CellStyle centerDataStyle = workbook.createCellStyle();
            centerDataStyle.setFont(dataFont);
            centerDataStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorder(centerDataStyle);

            // Header row starting directly at row 0
            String[] headers = {"DATE &TIME", "EVENT TYPE", "DESCRIPTION", "IP ADDRESS"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(EXCEL_DATE_FORMAT);

            int rowIdx = 1;
            for (ActivityLog log : logs) {
                Row row = sheet.createRow(rowIdx++);

                Cell cellTime = row.createCell(0);
                cellTime.setCellValue(log.getCreatedAt() != null ? log.getCreatedAt().format(formatter) : "");
                cellTime.setCellStyle(centerDataStyle);

                Cell cellType = row.createCell(1);
                cellType.setCellValue(log.getEventType() != null ? log.getEventType().name() : "");
                cellType.setCellStyle(centerDataStyle);

                Cell cellDesc = row.createCell(2);
                cellDesc.setCellValue(log.getDescription() != null ? log.getDescription() : "");
                cellDesc.setCellStyle(dataStyle);

                Cell cellIp = row.createCell(3);
                cellIp.setCellValue(log.getIpAddress() != null ? log.getIpAddress() : "");
                cellIp.setCellStyle(centerDataStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, currentWidth + 1024);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to export Excel file", e);
        }
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
