package org.example.service.impl;

import com.opencsv.CSVReader;
import org.example.service.CsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CsvServiceImpl implements CsvService {
    private static final Logger logger = LoggerFactory.getLogger(CsvServiceImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void importCsv(MultipartFile file, String tableName) throws Exception {
        logger.info("开始导入CSV文件到表: {}", tableName);
        
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件为空");
        }

        if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("请上传CSV文件");
        }

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVReader csvReader = new CSVReader(reader)) {

            // 读取CSV头部
            String[] headers = csvReader.readNext();
            if (headers == null) {
                throw new IllegalArgumentException("CSV文件为空或格式不正确");
            }
            logger.info("CSV文件头部: {}", String.join(", ", headers));

            // 验证表是否存在
            String checkTableSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
            int tableCount = jdbcTemplate.queryForObject(checkTableSql, Integer.class, tableName);
            logger.info("表 {} 存在检查结果: {}", tableName, tableCount > 0 ? "存在" : "不存在");
            
            if (tableCount == 0) {
                throw new IllegalArgumentException("表 " + tableName + " 不存在");
            }

            // 清空表数据
            logger.info("清空表 {} 的现有数据", tableName);
            jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
            logger.info("表 {} 已清空", tableName);

            // 获取数据库表中的列名
            String checkColumnsSql = "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
            List<String> existingColumns = jdbcTemplate.queryForList(checkColumnsSql, String.class, tableName);
            logger.info("表 {} 的列: {}", tableName, existingColumns);

            // 创建列名映射
            Map<String, String> columnMapping = new HashMap<>();
            for (String header : headers) {
                // 移除引号和BOM标记
                String cleanHeader = header.replace("\"", "").replace("\uFEFF", "").trim();
                
                logger.info("列名映射: {} -> {}", cleanHeader, cleanHeader);
                
                if (!existingColumns.contains(cleanHeader)) {
                    logger.error("列名不匹配 - CSV列名: {}, 数据库中的列: {}", 
                        cleanHeader, existingColumns);
                    throw new IllegalArgumentException("列 " + cleanHeader + " 在表 " + tableName + " 中不存在");
                }
                columnMapping.put(cleanHeader, cleanHeader);
            }

            // 构建插入SQL
            StringBuilder insertSql = new StringBuilder("INSERT INTO " + tableName + " (");
            List<String> dbColumnNames = new ArrayList<>();
            for (String header : headers) {
                String cleanHeader = header.replace("\"", "").replace("\uFEFF", "").trim();
                // 使用反引号包裹列名，以处理特殊字符
                dbColumnNames.add("`" + columnMapping.get(cleanHeader) + "`");
            }
            insertSql.append(String.join(", ", dbColumnNames));
            insertSql.append(") VALUES (");

            // 添加占位符
            for (int i = 0; i < headers.length; i++) {
                insertSql.append("?");
                if (i < headers.length - 1) {
                    insertSql.append(", ");
                }
            }
            insertSql.append(")");

            String finalSql = insertSql.toString();
            logger.info("生成的SQL语句: {}", finalSql);

            // 批量读取数据
            List<Object[]> batchData = new ArrayList<>();
            String[] nextLine;
            int rowCount = 0;
            int errorCount = 0;
            while ((nextLine = csvReader.readNext()) != null) {
                if (nextLine.length == headers.length) {
                    // 确保所有值都不为null
                    Object[] rowData = new Object[headers.length];
                    for (int i = 0; i < nextLine.length; i++) {
                        rowData[i] = nextLine[i] != null ? nextLine[i].trim() : "";
                    }
                    batchData.add(rowData);
                    rowCount++;
                } else {
                    logger.warn("跳过不匹配的行: {}", String.join(", ", nextLine));
                    errorCount++;
                }
            }

            // 执行批量插入
            if (!batchData.isEmpty()) {
                try {
                    int[] results = jdbcTemplate.batchUpdate(finalSql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(java.sql.PreparedStatement ps, int i) throws java.sql.SQLException {
                            Object[] rowData = batchData.get(i);
                            for (int j = 0; j < rowData.length; j++) {
                                String value = (String) rowData[j];
                                ps.setString(j + 1, value != null ? value : "");
                            }
                        }

                        @Override
                        public int getBatchSize() {
                            return batchData.size();
                        }
                    });
                    logger.info("成功导入 {} 行数据到表 {}, 跳过 {} 行无效数据", rowCount, tableName, errorCount);
                } catch (Exception e) {
                    logger.error("批量插入数据失败: {}", e.getMessage(), e);
                    throw new RuntimeException("导入数据失败: " + e.getMessage(), e);
                }
            } else {
                throw new IllegalArgumentException("CSV文件中没有有效数据");
            }
        } catch (Exception e) {
            logger.error("CSV导入失败: {}", e.getMessage(), e);
            throw new RuntimeException("CSV导入失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> getAllTables() {
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    @Override
    public List<Map<String, Object>> getTableData(String tableName, int page, int size) {
        String sql = "SELECT * FROM " + tableName + " LIMIT ? OFFSET ?";
        return jdbcTemplate.queryForList(sql, size, page * size);
    }

    @Override
    public long getTableCount(String tableName) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
} 