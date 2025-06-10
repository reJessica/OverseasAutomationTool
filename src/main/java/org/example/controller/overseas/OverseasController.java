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

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/overseas")
public class OverseasController {
    private static final Logger logger = LoggerFactory.getLogger(OverseasController.class);
    private static final String UPLOAD_DIR = "uploads";

    @Autowired
    private OverseasReportService reportService;

    @GetMapping("/table")
    public String showReportTable(Model model) {
        try {
            List<OverseasReport> reports = reportService.getAllReports();
            model.addAttribute("reports", reports);
            return "overseas-table";
        } catch (Exception e) {
            logger.error("获取报告列表失败", e);
            model.addAttribute("error", "获取报告列表失败");
            return "error";
        }
    }

    @GetMapping("/report/detail/{id}")
    public String showReportDetail(@PathVariable Long id, Model model) {
        try {
            OverseasReport report = reportService.getReportById(id);
            if (report == null) {
                return "redirect:/overseas/table";
            }
            model.addAttribute("report", report);
            return "overseas-report-detail";
        } catch (Exception e) {
            logger.error("获取报告详情失败", e);
            model.addAttribute("error", "获取报告详情失败");
            return "error";
        }
    }

    @PostMapping("/report/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateReport(@PathVariable Long id, @RequestBody OverseasReport updatedReport) {
        Map<String, Object> response = new HashMap<>();
        try {
            OverseasReport existingReport = reportService.getReportById(id);
            if (existingReport == null) {
                response.put("success", false);
                response.put("message", "报告不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            existingReport.setReportDate(updatedReport.getReportDate());
            reportService.updateReport(id, existingReport);
            
            response.put("success", true);
            response.put("message", "更新成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("更新报告失败", e);
            response.put("success", false);
            response.put("message", "更新失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/report/preview")
    public void previewReport(@RequestParam String path, HttpServletResponse response) throws IOException {
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

    @GetMapping("/report/download")
    public void downloadReport(@RequestParam String path, HttpServletResponse response) throws IOException {
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

    @PostMapping("/report/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> saveReport(@RequestBody OverseasReport report) {
        Map<String, Object> response = new HashMap<>();
        try {
            reportService.saveReport(report);
            response.put("success", true);
            response.put("message", "保存成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("保存报告失败", e);
            response.put("success", false);
            response.put("message", "保存失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/report/{reportNumber}")
    @ResponseBody
    public ResponseEntity<OverseasReport> getReport(@PathVariable String reportNumber) {
        try {
            OverseasReport report = reportService.getReportByNumber(reportNumber);
            if (report == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            logger.error("获取报告失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/report/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateReportStatus(@RequestParam Long id, @RequestParam String status) {
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

    @DeleteMapping("/report/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteReport(@PathVariable Long id) {
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
} 