package org.example.controller.overseas;

import org.example.entity.OverseasReport;
import org.example.service.OverseasReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/overseas")
public class OverseasController {
    private static final Logger logger = LoggerFactory.getLogger(OverseasController.class);
    private static final String UPLOAD_DIR = "uploads";

    @Autowired
    private OverseasReportService reportService;

    // 页面路由
    @GetMapping("/table")
    public String showReportTable() {
        return "pages/overseas-table";
    }

    @GetMapping("/add")
    public String showAddReportForm() {
        return "pages/overseas-report-add";
    }

    @GetMapping("/detail/{id}")
    public String showReportDetail(@PathVariable Long id, Model model) {
        model.addAttribute("reportId", id);
        return "pages/overseas-report-detail";
    }

    // API接口
    @GetMapping("/api/report/list")
    @ResponseBody
    public ResponseEntity<?> getReportList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("获取报告列表，页码：{}，每页大小：{}", page, size);
            
            List<OverseasReport> reports = reportService.getAllReports(page, size);
            long total = reportService.getTotalReports();
            
            Map<String, Object> response = new HashMap<>();
            response.put("reports", reports);
            response.put("currentPage", page);
            response.put("totalPages", (int) Math.ceil((double) total / size));
            response.put("totalItems", total);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取报告列表失败", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/api/report/info/{id}")
    @ResponseBody
    public ResponseEntity<?> getReportInfo(@PathVariable Long id) {
        logger.info("获取报告详情，报告ID：{}", id);
        try {
            OverseasReport report = reportService.getReportById(id);
            if (report == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "报告不存在"));
            }
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("获取报告详情失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "获取报告详情失败：" + e.getMessage()));
        }
    }

    @PostMapping("/api/report/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createReport(@RequestBody Map<String, Object> reportData) {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("新增报告请求，数据：{}", reportData);
            
            if (reportData.get("report_no") == null || reportData.get("report_no").toString().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "报告编号不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            OverseasReport report = convertMapToReport(reportData);
            
            try {
                // 检查报告编号是否已存在
                OverseasReport existingReport = reportService.getReportByNumber(report.getReportNo());
                if (existingReport != null) {
                    response.put("success", false);
                    response.put("message", "报告编号已存在：" + report.getReportNo());
                    return ResponseEntity.badRequest().body(response);
                }
            } catch (Exception e) {
                // 如果获取报告失败，说明报告不存在，可以继续创建
                logger.info("报告编号 {} 不存在，可以创建新报告", report.getReportNo());
            }
            
            report.setCreatedAt(LocalDateTime.now());
            report.setUpdatedAt(LocalDateTime.now());
            report.setStatus("ACTIVE");
            
            reportService.saveReport(report);
            
            response.put("success", true);
            response.put("message", "报告新增成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("新增报告失败", e);
            response.put("success", false);
            response.put("message", "新增失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/api/report/modify/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> modifyReport(@PathVariable Long id, @RequestBody Map<String, Object> reportData) {
        Map<String, Object> response = new HashMap<>();
        try {
            OverseasReport existingReport = reportService.getReportById(id);
            if (existingReport == null) {
                response.put("success", false);
                response.put("message", "报告不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            OverseasReport updatedReport = convertMapToReport(reportData);
            updatedReport.setId(id);
            updatedReport.setUpdatedAt(LocalDateTime.now());
            
            reportService.updateReport(id, updatedReport);
            
            response.put("success", true);
            response.put("message", "报告更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("更新报告失败", e);
            response.put("success", false);
            response.put("message", "更新失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/api/report/remove/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeReport(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            reportService.deleteReport(id);
            response.put("success", true);
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("删除报告失败", e);
            response.put("success", false);
            response.put("message", "删除失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/api/report/status/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> changeReportStatus(
            @PathVariable Long id, 
            @RequestParam String status) {
        Map<String, Object> response = new HashMap<>();
        try {
            reportService.updateReportStatus(id, status);
            response.put("success", true);
            response.put("message", "状态更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("更新报告状态失败", e);
            response.put("success", false);
            response.put("message", "更新失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 文件处理相关接口
    @GetMapping("/api/report/view")
    public void viewReport(@RequestParam String path, HttpServletResponse response) throws IOException {
        try {
            Path filePath = Paths.get(UPLOAD_DIR, path).normalize();
            if (!filePath.startsWith(Paths.get(UPLOAD_DIR))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "访问被拒绝");
                return;
            }

            File file = filePath.toFile();
            if (!file.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String contentType = Files.probeContentType(filePath);
            response.setContentType(contentType != null ? contentType : "application/octet-stream");
            response.setHeader("Content-Disposition", "inline; filename=" + file.getName());
            
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            logger.error("预览文件失败", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/api/report/export")
    public void exportReport(@RequestParam String path, HttpServletResponse response) throws IOException {
        try {
            Path filePath = Paths.get(UPLOAD_DIR, path).normalize();
            if (!filePath.startsWith(Paths.get(UPLOAD_DIR))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "访问被拒绝");
                return;
            }

            File file = filePath.toFile();
            if (!file.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String contentType = Files.probeContentType(filePath);
            response.setContentType(contentType != null ? contentType : "application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
            
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            logger.error("下载文件失败", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // 辅助方法：解析LocalDate
    private LocalDate parseLocalDate(Object value) {
        if (value == null) return null;
        try {
            return LocalDate.parse(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    // 辅助方法：将Map转换为OverseasReport对象
    private OverseasReport convertMapToReport(Map<String, Object> map) {
        OverseasReport report = new OverseasReport();
        // 基本信息
        report.setReportNo((String) map.get("report_no"));
        report.setReportNoEn((String) map.get("report_no_en"));
        report.setReportPath((String) map.get("report_path"));
        report.setStatus((String) map.get("status"));
        report.setReportDate(parseLocalDate(map.get("report_date")));
        report.setReportDateEn(parseLocalDate(map.get("report_date_en")));
        report.setReporter((String) map.get("reporter"));
        report.setReporterEn((String) map.get("reporter_en"));
        report.setCustomerName((String) map.get("customer_name"));
        report.setCustomerNameEn((String) map.get("customer_name_en"));
        report.setAddress((String) map.get("address"));
        report.setAddressEn((String) map.get("address_en"));
        report.setContactPerson((String) map.get("contact_person"));
        report.setContactPersonEn((String) map.get("contact_person_en"));
        report.setTel((String) map.get("tel"));
        report.setTelEn((String) map.get("tel_en"));
        report.setOccurrencePlace((String) map.get("occurrence_place"));
        report.setOccurrencePlaceEn((String) map.get("occurrence_place_en"));
        // 医疗器械情况
        report.setProductName((String) map.get("product_name"));
        report.setProductNameEn((String) map.get("product_name_en"));
        report.setRegistrationNo((String) map.get("registration_no"));
        report.setRegistrationNoEn((String) map.get("registration_no_en"));
        report.setModule((String) map.get("module"));
        report.setModuleEn((String) map.get("module_en"));
        report.setProductPackage((String) map.get("package"));
        report.setProductPackageEn((String) map.get("package_en"));
        report.setOriginCountry((String) map.get("origin_country"));
        report.setOriginCountryEn((String) map.get("origin_country_en"));
        report.setClassType((String) map.get("class_type"));
        report.setClassTypeEn((String) map.get("class_type_en"));
        report.setProductType((String) map.get("product_type"));
        report.setProductTypeEn((String) map.get("product_type_en"));
        report.setProductLot((String) map.get("product_lot"));
        report.setProductLotEn((String) map.get("product_lot_en"));
        report.setProductNo((String) map.get("product_no"));
        report.setProductNoEn((String) map.get("product_no_en"));
        report.setUdi((String) map.get("udi"));
        report.setUdiEn((String) map.get("udi_en"));
        report.setManufacturingDate(parseLocalDate(map.get("manufacturing_date")));
        report.setManufacturingDateEn(parseLocalDate(map.get("manufacturing_date_en")));
        report.setExpirationDate(parseLocalDate(map.get("expiration_date")));
        report.setExpirationDateEn(parseLocalDate(map.get("expiration_date_en")));
        // 不良事件情况
        report.setEventOccurrenceDate(parseLocalDate(map.get("event_occurrence_date")));
        report.setEventOccurrenceDateEn(parseLocalDate(map.get("event_occurrence_date_en")));
        report.setKnowledgeDate(parseLocalDate(map.get("knowledge_date")));
        report.setKnowledgeDateEn(parseLocalDate(map.get("knowledge_date_en")));
        report.setInjuryType((String) map.get("injury_type"));
        report.setInjuryTypeEn((String) map.get("injury_type_en"));
        report.setInjury((String) map.get("injury"));
        report.setInjuryEn((String) map.get("injury_en"));
        report.setDeviceMalfunctionDesc((String) map.get("device_malfunction_desc"));
        report.setDeviceMalfunctionDescEn((String) map.get("device_malfunction_desc_en"));
        report.setPatientName((String) map.get("patient_name"));
        report.setPatientNameEn((String) map.get("patient_name_en"));
        report.setBirthDate(parseLocalDate(map.get("birth_date")));
        report.setBirthDateEn(parseLocalDate(map.get("birth_date_en")));
        report.setAge((String) map.get("age"));
        report.setAgeEn((String) map.get("age_en"));
        report.setGender((String) map.get("gender"));
        report.setGenderEn((String) map.get("gender_en"));
        report.setMedicalRecordNo((String) map.get("medical_record_no"));
        report.setMedicalRecordNoEn((String) map.get("medical_record_no_en"));
        report.setMedicalHistory((String) map.get("medical_history"));
        report.setMedicalHistoryEn((String) map.get("medical_history_en"));
        // 使用情况
        report.setDiseaseIntended((String) map.get("disease_intended"));
        report.setDiseaseIntendedEn((String) map.get("disease_intended_en"));
        report.setUsageDate(parseLocalDate(map.get("usage_date")));
        report.setUsageDateEn(parseLocalDate(map.get("usage_date_en")));
        report.setUsageSite((String) map.get("usage_site"));
        report.setUsageSiteEn((String) map.get("usage_site_en"));
        report.setInstitutionName((String) map.get("institution_name"));
        report.setInstitutionNameEn((String) map.get("institution_name_en"));
        report.setUsageProcess((String) map.get("usage_process"));
        report.setUsageProcessEn((String) map.get("usage_process_en"));
        report.setDrugDeviceCombDesc((String) map.get("drug_device_comb_desc"));
        report.setDrugDeviceCombDescEn((String) map.get("drug_device_comb_desc_en"));
        // 事件调查
        report.setInvestigationFlag((String) map.get("investigation_flag"));
        report.setInvestigationFlagEn((String) map.get("investigation_flag_en"));
        report.setInvestigationDesc((String) map.get("investigation_desc"));
        report.setInvestigationDescEn((String) map.get("investigation_desc_en"));
        // 评价结果
        report.setRelativeEvaluation((String) map.get("relative_evaluation"));
        report.setRelativeEvaluationEn((String) map.get("relative_evaluation_en"));
        report.setEventReasonAnalysis((String) map.get("event_reason_analysis"));
        report.setEventReasonAnalysisEn((String) map.get("event_reason_analysis_en"));
        report.setNeedRiskAssessment((String) map.get("need_risk_assessment"));
        report.setNeedRiskAssessmentEn((String) map.get("need_risk_assessment_en"));
        report.setPlanSubmitDate(parseLocalDate(map.get("plan_submit_date")));
        report.setPlanSubmitDateEn(parseLocalDate(map.get("plan_submit_date_en")));
        // 控制措施
        report.setHasControlMeasure((String) map.get("has_control_measure"));
        report.setHasControlMeasureEn((String) map.get("has_control_measure_en"));
        report.setControlMeasureDetails((String) map.get("control_measure_details"));
        report.setControlMeasureDetailsEn((String) map.get("control_measure_details_en"));
        report.setNoControlMeasureReason((String) map.get("no_control_measure_reason"));
        report.setNoControlMeasureReasonEn((String) map.get("no_control_measure_reason_en"));
        
        return report;
    }
}