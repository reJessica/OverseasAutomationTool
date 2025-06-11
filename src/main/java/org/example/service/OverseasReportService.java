package org.example.service;

import org.example.entity.OverseasReport;
import org.example.mapper.OverseasReportMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OverseasReportService {
    private static final Logger logger = LoggerFactory.getLogger(OverseasReportService.class);

    @Autowired
    private OverseasReportMapper overseasReportMapper;

    public List<OverseasReport> getAllReports(int page, int size) {
        try {
            logger.info("开始获取报告列表，页码：{}，每页大小：{}", page, size);
            int offset = page * size;
            List<OverseasReport> reports = overseasReportMapper.findAll(offset, size);
            logger.info("成功获取到 {} 条报告记录", reports.size());
            return reports;
        } catch (Exception e) {
            logger.error("获取报告列表失败", e);
            throw new RuntimeException("获取报告列表失败：" + e.getMessage());
        }
    }

    public long getTotalReports() {
        try {
            return overseasReportMapper.count();
        } catch (Exception e) {
            logger.error("获取报告总数失败", e);
            throw new RuntimeException("获取报告总数失败：" + e.getMessage());
        }
    }

    @Transactional
    public void saveReport(OverseasReport report) {
        try {
            logger.info("开始保存报告：{}", report.getReportNo());
            overseasReportMapper.insert(report);
            logger.info("报告保存成功");
        } catch (Exception e) {
            logger.error("保存报告失败", e);
            throw new RuntimeException("保存报告失败：" + e.getMessage());
        }
    }

    public OverseasReport getReportById(Long id) {
        try {
            logger.info("开始获取报告，ID：{}", id);
            OverseasReport report = overseasReportMapper.findById(id);
            if (report == null) {
                logger.warn("未找到报告，ID：{}", id);
                throw new RuntimeException("报告不存在");
            }
            logger.info("成功获取报告：{}", report.getReportNo());
            return report;
        } catch (Exception e) {
            logger.error("获取报告失败", e);
            throw new RuntimeException("获取报告失败：" + e.getMessage());
        }
    }

    public OverseasReport getReportByNumber(String reportNumber) {
        try {
            logger.info("开始获取报告，报告编号：{}", reportNumber);
            OverseasReport report = overseasReportMapper.findByReportNumber(reportNumber);
            if (report == null) {
                logger.warn("未找到报告，报告编号：{}", reportNumber);
                return null;
            }
            logger.info("成功获取报告：{}", report.getReportNo());
            return report;
        } catch (Exception e) {
            logger.error("获取报告失败", e);
            throw new RuntimeException("获取报告失败：" + e.getMessage());
        }
    }

    @Transactional
    public void updateReportStatus(Long id, String status) {
        try {
            logger.info("开始更新报告状态，ID：{}，新状态：{}", id, status);
            overseasReportMapper.updateStatus(id, status);
            logger.info("报告状态更新成功");
        } catch (Exception e) {
            logger.error("更新报告状态失败", e);
            throw new RuntimeException("更新报告状态失败：" + e.getMessage());
        }
    }

    @Transactional
    public void deleteReport(Long id) {
        try {
            logger.info("开始删除报告，ID：{}", id);
            overseasReportMapper.deleteById(id);
            logger.info("报告删除成功");
        } catch (Exception e) {
            logger.error("删除报告失败", e);
            throw new RuntimeException("删除报告失败：" + e.getMessage());
        }
    }

    @Transactional
    public void updateReport(Long id, OverseasReport report) {
        try {
            logger.info("开始更新报告，ID：{}", id);
            
            // 检查报告编号是否已存在（排除当前报告）
            OverseasReport existingReport = overseasReportMapper.findByReportNumber(report.getReportNo());
            if (existingReport != null && !existingReport.getId().equals(id)) {
                logger.warn("报告编号已存在：{}", report.getReportNo());
                throw new RuntimeException("报告编号已存在：" + report.getReportNo());
            }
            
            overseasReportMapper.update(report);
            logger.info("报告更新成功");
        } catch (Exception e) {
            logger.error("更新报告失败", e);
            throw new RuntimeException("更新报告失败：" + e.getMessage());
        }
    }
} 