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
import java.util.Collections;

import javax.servlet.http.HttpServletResponse;
import com.alibaba.excel.EasyExcel;
import javax.servlet.ServletOutputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.ArrayList;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

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
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "true") boolean exactMatch) {
        try {
            logger.info("获取报告列表，页码：{}，每页大小：{}，搜索文本：{}，精确匹配：{}", page, size, search, exactMatch);
            
            List<OverseasReport> reports;
            long total;
            
            if (search != null && !search.trim().isEmpty()) {
                reports = reportService.searchReports(search.trim(), exactMatch, page, size);
                total = reportService.getTotalSearchResults(search.trim(), exactMatch);
            } else {
                reports = reportService.getAllReports(page, size);
                total = reportService.getTotalReports();
            }
            
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

    @GetMapping("/api/report/auto-fill-product")
    @ResponseBody
    public ResponseEntity<?> autoFillProductInfo(@RequestParam String productNo) {
        try {
            logger.info("自动填写产品信息，产品编号：{}", productNo);
            
            // 从material_list表中查询产品信息
            Map<String, Object> productInfo = reportService.getProductInfoByNo(productNo);
            
            if (productInfo == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "未找到对应的产品信息"
                    ));
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", productInfo
            ));
        } catch (Exception e) {
            logger.error("获取产品信息失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "获取产品信息失败：" + e.getMessage()
                ));
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
            report.setStatus("PENDING");
            
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

    @PostMapping("/api/report/batch-export")
    public void batchExport(@RequestBody List<Long> ids, HttpServletResponse response) throws IOException {
        List<OverseasReport> reports = reportService.findByIds(ids);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String fileName = URLEncoder.encode("报告批量导出.xlsx", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        ServletOutputStream out = response.getOutputStream();

        // 1. 第一行：分组大类
        List<String> groupRow = Arrays.asList(
            "医疗器械情况", "医疗器械情况", "医疗器械情况", "医疗器械情况", "医疗器械情况", "医疗器械情况", "医疗器械情况", "医疗器械情况",
            "医疗器械情况", "医疗器械情况", "不良事件情况", "不良事件情况", "不良事件情况", "不良事件情况", "不良事件情况", "不良事件情况", "不良事件情况", "不良事件情况", "不良事件情况", "不良事件情况",
            "不良事件情况", "不良事件情况", "使用情况", "使用情况", "使用情况", "使用情况", "使用情况", "事件调查", "事件调查", "评价结果", "评价结果", "评价结果", "评价结果", "控制措施", "控制措施", "控制措施"
        );
        // 2. 第二行：表头
        List<String> headerRow = Arrays.asList(
            "产品名称*", "注册证编号*", "产地*", "管理类别*", "产品类别*", "产品批号*", "产品编号*", "UDI",
            "生产日期(yyyy-MM-dd)", "有效期至(yyyy-MM-dd)", "事件发生日期*(yyyy-MM-dd)", "发现或获知日期*(yyyy-MM-dd)",
            "伤害*", "伤害表现*", "姓名", "出生日期(yyyy-MM-dd)", "年龄单位", "年龄", "性别", "病历号", "既往病史",
            "器械故障表现*", "预期治疗疾病或作用", "器械使用日期*(yyyy-MM-dd)", "使用场所*", "场所名称*", "使用过程*",
            "合并用药/械情况说明", "是否展开了调查*", "调查情况*", "关联性评价*", "事件原因分析*", "是否需要开展产品风险评价*",
            "计划提交时间(yyyy-MM-dd)", "是否采取了控制措施*", "具体控制措施描述*", "未采取控制措施原因*"
        );

        List<List<String>> dataList = new ArrayList<>();
        dataList.add(groupRow);
        dataList.add(headerRow);

        // 3. 数据行
        for (OverseasReport r : reports) {
            dataList.add(Arrays.asList(
                n(r.getProductName()), n(r.getRegistrationNo()), n(r.getOriginCountry()), n(r.getClassType()), n(r.getProductType()),
                n(r.getProductLot()), n(r.getProductNo()), n(r.getUdi()),
                d(r.getManufacturingDate()), d(r.getExpirationDate()), d(r.getEventOccurrenceDate()), d(r.getKnowledgeDate()),
                n(r.getInjuryType()), n(r.getInjury()), n(r.getPatientName()), d(r.getBirthDate()), n(r.getAgeEn()), n(r.getAge()), n(r.getGender()), n(r.getMedicalRecordNo()), n(r.getMedicalHistory()),
                n(r.getDeviceMalfunctionDesc()), n(r.getDiseaseIntended()), d(r.getUsageDate()), n(r.getUsageSite()), n(r.getInstitutionName()), n(r.getUsageProcess()),
                n(r.getDrugDeviceCombDesc()), n(r.getInvestigationFlag()), n(r.getInvestigationDesc()), n(r.getRelativeEvaluation()), n(r.getEventReasonAnalysis()), n(r.getNeedRiskAssessment()),
                d(r.getPlanSubmitDate()), n(r.getHasControlMeasure()), n(r.getControlMeasureDetails()), n(r.getNoControlMeasureReason())
            ));
        }

        // 4. 合并单元格
        // 每个大类的起止列（示例，需根据实际表头调整）
        List<int[]> mergeRegions = Arrays.asList(
            new int[]{0, 9},   // 医疗器械情况
            new int[]{10, 21}, // 不良事件情况
            new int[]{22, 27}, // 使用情况
            new int[]{28, 29}, // 事件调查
            new int[]{30, 33}, // 评价结果
            new int[]{34, 36}  // 控制措施
        );

        // 写入Excel
        EasyExcel.write(out)
            .sheet("报告导出")
            .registerWriteHandler(new com.alibaba.excel.write.handler.SheetWriteHandler() {
                @Override
                public void beforeSheetCreate(com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder writeWorkbookHolder, com.alibaba.excel.write.metadata.holder.WriteSheetHolder writeSheetHolder) {}
                @Override
                public void afterSheetCreate(com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder writeWorkbookHolder, com.alibaba.excel.write.metadata.holder.WriteSheetHolder writeSheetHolder) {
                    org.apache.poi.ss.usermodel.Sheet sheet = writeSheetHolder.getSheet();
                    // 合并大类单元格
                    for (int[] region : mergeRegions) {
                        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, region[0], region[1]));
                    }
                    // 设置列宽
                    for (int i = 0; i < headerRow.size(); i++) {
                        sheet.setColumnWidth(i, 20 * 256); // 20字符宽
                    }
                }
            })
            .doWrite(dataList);

        out.flush();
        out.close();
    }

    @PostMapping("/api/report/export-template")
    public void exportTemplate(@RequestBody Map<String, Object> reportData, HttpServletResponse response) throws IOException {
        try {
            // 读取模板文件
            String templatePath = "src/main/resources/report_template.xlsx";
            File templateFile = new File(templatePath);
            if (!templateFile.exists()) {
                throw new IOException("模板文件不存在：" + templatePath);
            }

            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("报告详情.xlsx", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            // 使用EasyExcel写入数据
            try (ServletOutputStream out = response.getOutputStream()) {
                // 创建一个新的Excel文件
                Workbook workbook = WorkbookFactory.create(templateFile);
                Sheet sheet = workbook.getSheetAt(0);

                // 填充数据
                fillCell(sheet, 5, 2, (String)reportData.get("pm_no")); // 报告编号
                fillCell(sheet, 8, 2, (String)reportData.get("report_date")); // 报告日期
                fillCell(sheet, 11, 2, (String)reportData.get("reporter")); // 报告人
                fillCell(sheet, 12, 2, (String)reportData.get("reporter_en")); // 报告人
                fillCell(sheet, 14, 2, (String)reportData.get("customer_name")); // 客户名称
                fillCell(sheet, 15, 2, (String)reportData.get("customer_name_en")); // 客户名称
                fillCell(sheet, 17, 2, (String)reportData.get("address")); // 联系地址
                fillCell(sheet, 18, 2, (String)reportData.get("address_en")); // 联系地址
                fillCell(sheet, 20, 2, (String)reportData.get("contact_person")); // 联系人
                fillCell(sheet, 21, 2, (String)reportData.get("contact_person_en")); // 联系人
                fillCell(sheet, 23, 2, (String)reportData.get("tel")); // 联系电话
                fillCell(sheet, 26, 2, (String)reportData.get("occurrence_place")); // 发生地
                fillCell(sheet, 27, 2, (String)reportData.get("occurrence_place_en")); // 发生地
                
                // 医疗器械情况
                fillCell(sheet, 31, 2, (String)reportData.get("product_name")); // 产品名称
                fillCell(sheet, 32, 2, (String)reportData.get("product_name_en")); // 产品名称
                fillCell(sheet, 34, 2, (String)reportData.get("registration_no")); // 注册证编号
                fillCell(sheet, 37, 2, (String)reportData.get("module")); // 型号
                fillCell(sheet, 40, 2, (String)reportData.get("product_package")); // 规格
                fillCell(sheet, 43, 2, (String)reportData.get("origin_country")); // 产地
                fillCell(sheet, 44, 2, (String)reportData.get("origin_country_en")); // 产地
                fillCell(sheet, 46, 2, (String)reportData.get("class_type")); // 管理类别
                fillCell(sheet, 47, 2, (String)reportData.get("class_type_en")); // 管理类别
                fillCell(sheet, 49, 2, (String)reportData.get("product_type")); // 产品类别
                fillCell(sheet, 50, 2, (String)reportData.get("product_type_en")); // 产品类别
                fillCell(sheet, 52, 2, (String)reportData.get("product_lot")); // 产品批号
                fillCell(sheet, 55, 2, (String)reportData.get("product_no")); // 产品编号
                fillCell(sheet, 58, 2, (String)reportData.get("udi")); // UDI
                fillCell(sheet, 60, 2, (String)reportData.get("manufacturing_date")); // 生产日期
                fillCell(sheet, 63, 2, (String)reportData.get("expiration_date")); // 有效期至
                
                // 不良事件情况
                fillCell(sheet, 68, 2, (String)reportData.get("event_occurrence_date")); // 事件发生日期
                fillCell(sheet, 71, 2, (String)reportData.get("knowledge_date")); // 发现或获知日期
                fillCell(sheet, 74, 2, (String)reportData.get("injury_type")); // 伤害程度
                fillCell(sheet, 75, 2, (String)reportData.get("injury_type_en")); // 伤害程度
                fillCell(sheet, 77, 2, (String)reportData.get("injury")); // 伤害表现
                fillCell(sheet, 78, 2, (String)reportData.get("injury_en")); // 伤害表现
                fillCell(sheet, 80, 2, (String)reportData.get("device_malfunction_desc")); // 器械故障表现
                fillCell(sheet, 81, 2, (String)reportData.get("device_malfunction_desc_en")); // 器械故障表现fillCell(sheet, 81, 2, (String)reportData.get("device_malfunction_desc_en")); // 器械故障表现
                fillCell(sheet, 83, 2, (String)reportData.get("patient_name")); // 姓名
                fillCell(sheet, 86, 2, (String)reportData.get("birth_date")); // 出生日期
                fillCell(sheet, 89, 2, (String)reportData.get("age")); // 年龄
                fillCell(sheet, 90, 2, (String)reportData.get("age_en")); // 年龄
                fillCell(sheet, 92, 2, (String)reportData.get("gender")); // 性别
                fillCell(sheet, 93, 2, (String)reportData.get("gender_en")); // 性别
                fillCell(sheet, 95, 2, (String)reportData.get("medical_record_no")); // 病历号
                fillCell(sheet, 96, 2, (String)reportData.get("medical_record_no_en")); // 病历号
                fillCell(sheet, 98, 2, (String)reportData.get("medical_history")); // 既往病史
                fillCell(sheet, 99, 2, (String)reportData.get("medical_history_en")); // 既往病史
                
                // 使用情况
                fillCell(sheet, 102, 2, (String)reportData.get("disease_intended")); // 预期治疗疾病或作用
                fillCell(sheet, 103, 2, (String)reportData.get("disease_intended_en")); // 预期治疗疾病或作用
                fillCell(sheet, 105, 2, (String)reportData.get("usage_date")); // 器械使用日期
                fillCell(sheet, 108, 2, (String)reportData.get("usage_site")); // 使用场所
                fillCell(sheet, 109, 2, (String)reportData.get("usage_site_en")); // 使用场所
                fillCell(sheet, 111, 2, (String)reportData.get("institution_name")); // 场所名称
                fillCell(sheet, 112, 2, (String)reportData.get("institution_name_en")); // 场所名称
                fillCell(sheet, 114, 2, (String)reportData.get("usage_process")); // 使用过程
                fillCell(sheet, 115, 2, (String)reportData.get("usage_process_en")); // 使用过程
                fillCell(sheet, 117, 2, (String)reportData.get("drug_device_comb_desc")); // 合并用药/械情况说明
                fillCell(sheet, 118, 2, (String)reportData.get("drug_device_comb_desc_en")); // 合并用药/械情况说明
                
                // 事件调查
                fillCell(sheet, 122, 2, (String)reportData.get("investigation_flag")); // 是否开展了调查
                fillCell(sheet, 123, 2, (String)reportData.get("investigation_flag_en")); // 是否开展了调查
                 fillCell(sheet, 125, 2, (String)reportData.get("investigation_desc")); // 调查情况
                fillCell(sheet, 126, 2, (String)reportData.get("investigation_desc_en")); // 调查情况
                
                // 评价结果
                fillCell(sheet, 130, 2, (String)reportData.get("relative_evaluation")); // 关联性评价\
                fillCell(sheet, 131, 2, (String)reportData.get("relative_evaluation_en")); // 关联性评价
                fillCell(sheet, 133, 2, (String)reportData.get("event_reason_analysis")); // 事件原因分析
                fillCell(sheet, 134, 2, (String)reportData.get("event_reason_analysis_en")); // 事件原因分析
                fillCell(sheet, 136, 2, (String)reportData.get("need_risk_assessment")); // 是否需要开展产品风险评价
                fillCell(sheet, 137, 2, (String)reportData.get("need_risk_assessment_en")); // 是否需要开展产品风险评价
                fillCell(sheet, 139, 2, (String)reportData.get("plan_submit_date")); // 计划提交时间
                fillCell(sheet, 140, 2, (String)reportData.get("plan_submit_date_en")); // 计划提交时间
                
                // 控制措施
                fillCell(sheet, 144, 2, (String)reportData.get("has_control_measure")); // 是否已采取控制措施
                fillCell(sheet, 145, 2, (String)reportData.get("has_control_measure_en")); // 是否已采取控制措施
                fillCell(sheet, 147, 2, (String)reportData.get("control_measure_details")); // 具体控制措施
                fillCell(sheet, 148, 2, (String)reportData.get("control_measure_details_en")); // 具体控制措施
                fillCell(sheet, 150, 2, (String)reportData.get("no_control_measure_reason")); // 未采取控制措施原因
                fillCell(sheet, 151, 2, (String)reportData.get("no_control_measure_reason_en")); // 未采取控制措施原因


                // 写入输出流
                workbook.write(out);
                workbook.close();
            }
        } catch (Exception e) {
            logger.error("导出报告失败", e);
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"导出失败：" + e.getMessage() + "\"}");
        }
    }

    private void fillCell(Sheet sheet, int rowNum, int colNum, String value) {
        if (value != null) {
            Row row = sheet.getRow(rowNum);
            if (row == null) {
                row = sheet.createRow(rowNum);
            }
            Cell cell = row.getCell(colNum);
            if (cell == null) {
                cell = row.createCell(colNum);
            }
            cell.setCellValue(value);
        }
    }

    private String n(Object o) { return o == null ? "" : o.toString(); }
    private String d(java.time.LocalDate d) { return d == null ? "" : d.toString(); }

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
        report.setPMNo((String) map.get("PM_no"));
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
        report.setProductPackage((String) map.get("product_package"));
        report.setProductPackageEn((String) map.get("product_package_en"));
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