package org.example.service.impl;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.service.FileImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class FileImportServiceImpl implements FileImportService {
    private static final Logger logger = LoggerFactory.getLogger(FileImportServiceImpl.class);
    private static final int BATCH_SIZE = 1000;
    private final AtomicBoolean isImporting = new AtomicBoolean(false);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final Object importLock = new Object();

    /**
     * 将特殊字符转换为下划线
     * @param input 输入字符串
     * @return 转换后的字符串
     */
    private String normalizeColumnName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "column_" + System.currentTimeMillis();
        }
        
        // 将特殊字符替换为下划线
        String normalized = input.trim()
            .replaceAll("[:\\s/\\\\()（）:-]", "_")  // 替换冒号、空格、斜杠、括号、连字符
            .replaceAll("'", "")                    // 移除单引号
            .replaceAll("\\.", "")                // 将点号替换为下划线
            .replaceAll("_+", "_")                 // 将多个连续的下划线替换为单个下划线
            .replaceAll("^_|_$", "")              // 移除开头和结尾的下划线
            .replaceAll("' · `", "");             // 移除其他特殊字符
        
        // 如果转换后为空，生成一个默认列名
        if (normalized.isEmpty()) {
            return "column_" + System.currentTimeMillis();
        }
        
        return normalized;
    }

    private InputStream removeBom(InputStream inputStream) throws IOException {
        byte[] bom = new byte[3];
        if (inputStream.read(bom) == 3 && bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
            return new ByteArrayInputStream(inputStream.readAllBytes());
        }
        inputStream.reset();
        return inputStream;
    }

    /**
     * 处理重复的列名
     * @param headers 原始表头数组
     * @return 处理后的表头数组
     */
    private String[] handleDuplicateHeaders(String[] headers) {
        Map<String, Integer> headerCount = new HashMap<>();
        String[] normalizedHeaders = new String[headers.length];
        
        for (int i = 0; i < headers.length; i++) {
            String normalizedHeader = normalizeColumnName(headers[i]);
            headerCount.put(normalizedHeader, headerCount.getOrDefault(normalizedHeader, 0) + 1);
            
            if (headerCount.get(normalizedHeader) > 1) {
                // 如果是重复的Title列，第二个改为Title_1
                if (normalizedHeader.equals("Title")) {
                    normalizedHeaders[i] = "Title_1";
                } else {
                    // 其他重复列名添加数字后缀
                    normalizedHeaders[i] = normalizedHeader + "_" + headerCount.get(normalizedHeader);
                }
            } else {
                normalizedHeaders[i] = normalizedHeader;
            }
        }
        
        return normalizedHeaders;
    }

    /**
     * 处理日期值
     * @param value 输入的日期字符串
     * @return 处理后的日期值
     */
    private Object handleDateValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return java.sql.Date.valueOf(value.trim());
        } catch (Exception e) {
            logger.warn("无效的日期格式: {}", value);
            return null;
        }
    }

    /**
     * 处理数据行
     * @param line 原始数据行
     * @param headers 表头
     * @return 处理后的数据行
     */
    private Object[] processRowData(String[] line, String[] headers) {
        Object[] rowData = new Object[line.length];
        for (int i = 0; i < line.length; i++) {
            String value = line[i] != null ? line[i].trim() : null;
            
            // 检查列名是否包含日期相关字段
            if (headers[i].toLowerCase().contains("date") || 
                headers[i].toLowerCase().contains("created") || 
                headers[i].toLowerCase().contains("modified")) {
                rowData[i] = handleDateValue(value);
            } else {
                rowData[i] = value;
            }
        }
        return rowData;
    }

    @Override
    public void importCsv(MultipartFile file, String tableName) throws Exception {
        if (!isImporting.compareAndSet(false, true)) {
            throw new IllegalStateException("已有导入任务正在进行中，请稍后再试");
        }
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("文件不能为空");
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                throw new IllegalArgumentException("只支持CSV文件");
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(removeBom(file.getInputStream()), StandardCharsets.UTF_8));
                 CSVReader csvReader = new CSVReader(reader)) {
                
                logger.info("开始清空表 {} 的数据", tableName);
                // 获取清空前的数据量
                Long beforeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
                logger.info("清空前表 {} 中的数据量: {}", tableName, beforeCount);
                
                jdbcTemplate.execute("DELETE FROM " + tableName);
                
                // 获取清空后的数据量
                Long afterCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
                logger.info("清空后表 {} 中的数据量: {}", tableName, afterCount);
                
                // 读取CSV头部并规范化列名
                String[] headers = csvReader.readNext();
                if (headers == null || headers.length == 0) {
                    throw new IllegalArgumentException("CSV文件格式不正确：缺少表头");
                }

                logger.info("原始CSV表头: {}", String.join(", ", headers));

                // 处理重复的列名
                String[] normalizedHeaders = handleDuplicateHeaders(headers);

                logger.info("规范化后的表头: {}", String.join(", ", normalizedHeaders));

                // 批量插入数据
                List<Object[]> batch = new ArrayList<>();
                String[] line;
                int count = 0;

                while ((line = csvReader.readNext()) != null) {
                    // 确保数据行长度与表头长度一致
                    if (line.length != headers.length) {
                        logger.warn("跳过不完整的数据行: 期望列数={}, 实际列数={}", headers.length, line.length);
                        continue;
                    }

                    // 处理数据行
                    Object[] rowData = processRowData(line, normalizedHeaders);
                    batch.add(rowData);
                    count++;

                    if (batch.size() >= BATCH_SIZE) {
                        logger.info("处理批次数据: 行数={}", batch.size());
                        insertBatch(tableName, normalizedHeaders, batch);
                        batch.clear();
                    }
                }

                if (!batch.isEmpty()) {
                    logger.info("处理最后一批数据: 行数={}", batch.size());
                    insertBatch(tableName, normalizedHeaders, batch);
                }

                logger.info("成功导入 {} 条数据到表 {}", count, tableName);
            } catch (Exception e) {
                logger.error("导入CSV文件失败", e);
                throw new Exception("导入CSV文件失败: " + e.getMessage());
            }
        } finally {
            isImporting.set(false);
        }
    }

    @Override
    public void importExcel(MultipartFile file, String tableName) throws Exception {
        if (!isImporting.compareAndSet(false, true)) {
            throw new IllegalStateException("已有导入任务正在进行中，请稍后再试");
        }
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("文件不能为空");
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
                throw new IllegalArgumentException("只支持XLSX格式的Excel文件");
            }

            try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
                Sheet sheet = workbook.getSheetAt(0);
                Row headerRow = sheet.getRow(0);

                if (headerRow == null) {
                    throw new IllegalArgumentException("Excel文件格式不正确：缺少表头");
                }

                logger.info("开始清空表 {} 的数据", tableName);
                // 获取清空前的数据量
                Long beforeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
                logger.info("清空前表 {} 中的数据量: {}", tableName, beforeCount);
                
                jdbcTemplate.execute("DELETE FROM " + tableName);
                
                // 获取清空后的数据量
                Long afterCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
                logger.info("清空后表 {} 中的数据量: {}", tableName, afterCount);
                
                // 获取表头并规范化列名
                List<String> headers = new ArrayList<>();
                for (Cell cell : headerRow) {
                    headers.add(normalizeColumnName(cell.getStringCellValue()));
                }

                // 批量插入数据
                List<Object[]> batch = new ArrayList<>();
                int count = 0;

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    Object[] rowData = new Object[headers.size()];
                    for (int j = 0; j < headers.size(); j++) {
                        Cell cell = row.getCell(j);
                        rowData[j] = getCellValue(cell);
                    }

                    batch.add(rowData);
                    count++;

                    if (batch.size() >= BATCH_SIZE) {
                        insertBatch(tableName, headers.toArray(new String[0]), batch);
                        batch.clear();
                    }
                }

                if (!batch.isEmpty()) {
                    insertBatch(tableName, headers.toArray(new String[0]), batch);
                }

                logger.info("成功导入 {} 条数据到表 {}", count, tableName);
            } catch (IOException e) {
                logger.error("导入Excel文件失败", e);
                throw new Exception("导入Excel文件失败: " + e.getMessage());
            }
        } finally {
            isImporting.set(false);
        }
    }

    private void insertBatch(String tableName, String[] headers, List<?> batch) {
        if (batch.isEmpty()) {
            logger.warn("批次数据为空，跳过插入");
            return;
        }

        // 验证数据
        for (Object row : batch) {
            if (!(row instanceof Object[])) {
                throw new IllegalArgumentException("数据格式不正确");
            }
            Object[] rowData = (Object[]) row;
            if (rowData.length != headers.length) {
                throw new IllegalArgumentException(
                    String.format("数据列数与表头列数不匹配: 数据列数=%d, 表头列数=%d", rowData.length, headers.length));
            }
        }

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < headers.length; i++) {
            if (i > 0) {
                placeholders.append(", ");
            }
            placeholders.append("?");
        }
        
        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName,
                String.join(", ", headers),
                placeholders.toString());
        
        logger.info("执行SQL: {}", sql);
        logger.info("参数数量: {}", headers.length);
        logger.info("批次大小: {}", batch.size());
        logger.info("表头: {}", String.join(", ", headers));
        
        try {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Object[] row = (Object[]) batch.get(i);
                    logger.debug("设置第{}行数据，列数: {}", i + 1, row.length);
                    for (int j = 0; j < row.length; j++) {
                        Object value = row[j];
                        if (value == null) {
                            ps.setNull(j + 1, java.sql.Types.VARCHAR);
                            logger.debug("列{}设置为NULL", j + 1);
                        } else {
                            ps.setString(j + 1, value.toString());
                            logger.debug("列{}设置为: {}", j + 1, value);
                        }
                    }
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        } catch (Exception e) {
            logger.error("批量插入数据失败: SQL={}, 错误={}", sql, e.getMessage());
            logger.error("错误详情:", e);
            throw new RuntimeException("批量插入数据失败: " + e.getMessage(), e);
        }
    }

    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return cell.getNumericCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private void createTable(String tableName, String[] headers) {
        StringBuilder columns = new StringBuilder();
        for (int i = 0; i < headers.length; i++) {
            if (i > 0) {
                columns.append(", ");
            }
            // 根据列名判断使用TEXT还是VARCHAR(255)
            if (headers[i].contains("Description") || 
                headers[i].contains("History") || 
                headers[i].contains("备注") || 
                headers[i].contains("变更") ||
                headers[i].contains("CER")) {
                columns.append(headers[i]).append(" TEXT");
            } else {
                columns.append(headers[i]).append(" VARCHAR(255)");
            }
        }
        String sql = String.format("CREATE TABLE %s (%s) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci", tableName, columns.toString());
        logger.info("创建表SQL: {}", sql);
        jdbcTemplate.execute(sql);
    }
} 