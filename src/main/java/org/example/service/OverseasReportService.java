package org.example.service;

import org.example.entity.OverseasReport;
import org.example.mapper.OverseasReportMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;
import java.util.Map;

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

    public List<OverseasReport> searchReports(String searchText, int page, int size) {
        return searchReports(searchText, true, page, size);
    }

    public List<OverseasReport> searchReports(String searchText, boolean exactMatch, int page, int size) {
        try {
            logger.info("开始搜索报告，搜索文本：{}，精确匹配：{}，页码：{}，每页大小：{}", searchText, exactMatch, page, size);
            int offset = page * size;
            List<OverseasReport> reports = overseasReportMapper.searchReports(searchText, exactMatch, offset, size);
            logger.info("搜索完成，找到 {} 条报告记录", reports.size());
            return reports;
        } catch (Exception e) {
            logger.error("搜索报告失败", e);
            throw new RuntimeException("搜索报告失败：" + e.getMessage());
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

    public long getTotalSearchResults(String searchText) {
        return getTotalSearchResults(searchText, true);
    }

    public long getTotalSearchResults(String searchText, boolean exactMatch) {
        try {
            return overseasReportMapper.countSearchResults(searchText, exactMatch);
        } catch (Exception e) {
            logger.error("获取搜索结果总数失败", e);
            throw new RuntimeException("获取搜索结果总数失败：" + e.getMessage());
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

    public Map<String, Object> getProductInfoByNo(String productNo) {
        try {
            logger.info("开始获取产品信息，产品编号：{}", productNo);
            Map<String, Object> productInfo = overseasReportMapper.getProductInfoByNo(productNo);
            if (productInfo == null) {
                logger.warn("未找到产品信息，产品编号：{}", productNo);
                return null;
            }
            
            // 设置默认值
            if (productInfo.get("origin_country") == null || productInfo.get("origin_country").toString().trim().isEmpty()) {
                productInfo.put("origin_country", "进口");
            }
            if (productInfo.get("origin_country_en") == null || productInfo.get("origin_country_en").toString().trim().isEmpty()) {
                productInfo.put("origin_country_en", "Import");
            }

            // 处理product_type的逻辑
            String productType = productInfo.get("product_type") != null ? productInfo.get("product_type").toString().trim() : "";
            String productName = productInfo.get("product_name") != null ? productInfo.get("product_name").toString() : "";
            
            logger.info("原始product_type: {}, 产品名称: {}", productType, productName);
            
            if ("耗材".equals(productType)) {
                productInfo.put("product_type", "体外诊断剂");
                productInfo.put("product_type_en", "IVD");
                logger.info("将product_type从'耗材'修改为'体外诊断剂'");
            } else {
                if (productName.contains("电极") || productName.contains("电解质")) {
                    productInfo.put("product_type", "无源");
                    productInfo.put("product_type_en", "Passive Power");
                    logger.info("根据产品名称'{}'，将product_type设置为'无源'", productName);
                } else {
                    productInfo.put("product_type", "有源");
                    productInfo.put("product_type_en", "Active Power");
                    logger.info("根据产品名称'{}'，将product_type设置为'有源'", productName);
                }
            }
            
            logger.info("最终product_type: {}", productInfo.get("product_type"));

            if (productInfo != null) {
                Map<String, Object> certInfo = overseasReportMapper.getCertificateInfoByNo(productNo);
                if (certInfo != null) {
                    String classType = (String) certInfo.get("class_type");
                    if ("1".equals(classType)) {
                        productInfo.put("class_type", "Ⅰ类");
                        productInfo.put("class_type_en", "Class Ⅰ");
                    } else if ("2".equals(classType)) {
                        productInfo.put("class_type", "Ⅱ类");
                        productInfo.put("class_type_en", "Class Ⅱ");
                    } else if ("3".equals(classType)) {
                        productInfo.put("class_type", "Ⅲ 类");
                        productInfo.put("class_type_en", "Class Ⅲ");
                    }
                }
            }

            return productInfo;
        } catch (Exception e) {
            logger.error("获取产品信息失败", e);
            throw new RuntimeException("获取产品信息失败：" + e.getMessage());
        }
    }

    public List<OverseasReport> findByIds(List<Long> ids) {
        try {
            logger.info("批量查找报告，ID列表：{}", ids);
            return overseasReportMapper.findByIds(ids);
        } catch (Exception e) {
            logger.error("批量查找报告失败", e);
            throw new RuntimeException("批量查找报告失败：" + e.getMessage());
        }
    }

    @Transactional
    public int updateReportCodes(List<Long> selectedIds, Sheet sheet) {
        try {
            int updatedCount = 0;
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // 获取reportId（第31列，索引为30）的前15个字符
                Cell idCell = row.getCell(30);
                if (idCell == null) continue;
                String idStr = idCell.getStringCellValue();
                if (idStr == null || idStr.trim().isEmpty()) continue;
                String reportId = idStr.trim().substring(0, Math.min(15, idStr.trim().length()));

                // 获取报告编码（第0列）
                Cell codeCell = row.getCell(0);
                if (codeCell == null) continue;
                String reportCode = codeCell.getStringCellValue();
                if (reportCode == null || reportCode.trim().isEmpty()) continue;

                overseasReportMapper.updateReportCode(reportId, reportCode.trim());
                updatedCount++;
            }
            return updatedCount;
        } catch (Exception e) {
            throw new RuntimeException("更新报告编码失败：" + e.getMessage());
        }
    }

    @Transactional
    public void updateReportCode(String reportNo, String reportCode) {
        overseasReportMapper.updateReportCode(reportNo, reportCode);
    }
} 