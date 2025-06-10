package org.example.service;

import org.example.entity.OverseasReport;
import org.example.mapper.OverseasReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OverseasReportService {

    @Autowired
    private OverseasReportMapper reportMapper;

    @Transactional
    public void saveReport(OverseasReport report) {
        // 设置创建和更新时间
        LocalDateTime now = LocalDateTime.now();
        report.setCreatedAt(now);
        report.setUpdatedAt(now);
        report.setStatus("ACTIVE");
        
        reportMapper.insert(report);
    }

    public List<OverseasReport> getAllReports() {
        return reportMapper.findAll();
    }

    public OverseasReport getReportById(Long id) {
        return reportMapper.findById(id);
    }

    public OverseasReport getReportByNumber(String reportNumber) {
        return reportMapper.findByReportNumber(reportNumber);
    }

    @Transactional
    public void updateReport(Long id, OverseasReport updatedReport) {
        OverseasReport existingReport = reportMapper.findById(id);
        if (existingReport != null) {
            // 更新所有字段
            updatedReport.setId(id);
            updatedReport.setUpdatedAt(LocalDateTime.now());
            reportMapper.update(updatedReport);
        }
    }

    @Transactional
    public void updateReportStatus(Long id, String status) {
        reportMapper.updateStatus(id, status);
    }

    @Transactional
    public void deleteReport(Long id) {
        reportMapper.deleteById(id);
    }

    private String generateReportNumber() {
        // 生成格式为：REP-YYYYMMDD-XXXXX的报告编号
        return String.format("REP-%s-%05d", 
            LocalDate.now().toString().replace("-", ""),
            (int)(Math.random() * 100000));
    }
} 