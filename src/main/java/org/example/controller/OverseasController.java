package org.example.controller;

import org.example.entity.OverseasReport;
import org.example.service.OverseasReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/overseas")
public class OverseasController {

    @Autowired
    private OverseasReportService reportService;

    @GetMapping("/table")
    public String showReportTable(Model model) {
        List<OverseasReport> reports = reportService.getAllReports();
        model.addAttribute("reports", reports);
        return "overseas-table";
    }

    @GetMapping("/report/detail/{id}")
    public String showReportDetail(@PathVariable Long id, Model model) {
        OverseasReport report = reportService.getReportById(id);
        if (report == null) {
            return "redirect:/overseas/table";
        }
        model.addAttribute("report", report);
        return "overseas-report-detail";
    }

    @PostMapping("/report/update/{id}")
    @ResponseBody
    public String updateReport(@PathVariable Long id, @RequestBody OverseasReport updatedReport) {
        try {
            OverseasReport existingReport = reportService.getReportById(id);
            if (existingReport == null) {
                return "报告不存在";
            }
            
            existingReport.setReportDate(updatedReport.getReportDate());
            existingReport.setStatus(updatedReport.getStatus());
            existingReport.setReportContent(updatedReport.getReportContent());
            
            reportService.updateReport(id, existingReport);
            return "success";
        } catch (Exception e) {
            return "更新失败：" + e.getMessage();
        }
    }

    @GetMapping("/report/preview")
    public void previewReport(@RequestParam String path, HttpServletResponse response) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "inline; filename=" + file.getName());
        
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    @GetMapping("/report/download")
    public void downloadReport(@RequestParam String path, HttpServletResponse response) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
        
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream out = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    @PostMapping("/report/save")
    @ResponseBody
    public String saveReport(@RequestParam String reportPath) {
        try {
            reportService.saveReport(reportPath);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @GetMapping("/report/{reportNumber}")
    @ResponseBody
    public OverseasReport getReport(@PathVariable String reportNumber) {
        return reportService.getReportByNumber(reportNumber);
    }

    @PostMapping("/report/status")
    @ResponseBody
    public String updateReportStatus(@RequestParam Long id, @RequestParam String status) {
        try {
            reportService.updateReportStatus(id, status);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @DeleteMapping("/report/{id}")
    @ResponseBody
    public String deleteReport(@PathVariable Long id) {
        try {
            reportService.deleteReport(id);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
} 