package org.example.util;

import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExcelToSqlConverter {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void convertExcelToSql(String excelFilePath, String tableName) throws Exception {
        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new Exception("Excel文件为空或格式不正确");
            }

            // 获取列名并处理特殊字符
            List<String> originalColumnNames = new ArrayList<>();
            Map<String, String> columnNameMapping = new HashMap<>();
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String originalName = cell.getStringCellValue().trim();
                    if (!originalName.isEmpty()) {
                        originalColumnNames.add(originalName);
                        // 将列名转换为安全的SQL标识符
                        String safeName = originalName.replace(":", "_")
                                .replace(" ", "_")
                                .replace("/", "_")
                                .replace("(", "_")
                                .replace(")", "_")
                                .replace("'", "_")
                                .replace(".", "_");
                        columnNameMapping.put(originalName, safeName);
                    }
                }
            }

            if (originalColumnNames.isEmpty()) {
                throw new Exception("未找到有效的列名");
            }

            // 验证表是否存在
            String checkTableSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
            int tableCount = jdbcTemplate.queryForObject(checkTableSql, Integer.class, tableName);
            if (tableCount == 0) {
                throw new Exception("表 " + tableName + " 不存在");
            }

            // 验证列是否存在
            String checkColumnsSql = "SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
            List<String> existingColumns = jdbcTemplate.queryForList(checkColumnsSql, String.class, tableName);
            
            for (String originalName : originalColumnNames) {
                String safeName = columnNameMapping.get(originalName);
                if (!existingColumns.contains(safeName)) {
                    throw new Exception("列 " + originalName + " (安全名称: " + safeName + ") 在表 " + tableName + " 中不存在");
                }
            }

            // 构建插入语句
            StringBuilder insertSql = new StringBuilder("INSERT INTO `" + tableName + "` (");
            List<String> safeColumnNames = new ArrayList<>();
            for (String originalName : originalColumnNames) {
                safeColumnNames.add("`" + columnNameMapping.get(originalName) + "`");
            }
            insertSql.append(String.join(", ", safeColumnNames));
            insertSql.append(") VALUES (");
            insertSql.append("?, ".repeat(originalColumnNames.size() - 1));
            insertSql.append("?)");

            // 准备批量插入的数据
            List<Object[]> batchData = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Object[] rowData = new Object[originalColumnNames.size()];
                boolean hasData = false;

                for (int j = 0; j < originalColumnNames.size(); j++) {
                    Cell cell = row.getCell(j);
                    Object value = getCellValue(cell);
                    rowData[j] = value;
                    if (value != null) {
                        hasData = true;
                    }
                }

                if (hasData) {
                    batchData.add(rowData);
                }
            }

            // 执行批量插入
            if (!batchData.isEmpty()) {
                try {
                    // 使用PreparedStatementCreator来确保参数正确绑定
                    jdbcTemplate.batchUpdate(insertSql.toString(), new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            Object[] rowData = batchData.get(i);
                            for (int j = 0; j < rowData.length; j++) {
                                Object value = rowData[j];
                                if (value == null) {
                                    ps.setNull(j + 1, java.sql.Types.VARCHAR);
                                } else if (value instanceof String) {
                                    ps.setString(j + 1, (String) value);
                                } else if (value instanceof Number) {
                                    ps.setObject(j + 1, value);
                                } else if (value instanceof java.util.Date) {
                                    ps.setTimestamp(j + 1, new java.sql.Timestamp(((java.util.Date) value).getTime()));
                                } else {
                                    ps.setString(j + 1, value.toString());
                                }
                            }
                        }

                        @Override
                        public int getBatchSize() {
                            return batchData.size();
                        }
                    });
                } catch (Exception e) {
                    throw new Exception("数据插入失败: " + e.getMessage() + "\nSQL: " + insertSql.toString());
                }
            } else {
                throw new Exception("Excel文件中没有有效数据");
            }
        }
    }

    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                String value = cell.getStringCellValue().trim();
                return value.isEmpty() ? null : value;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                double numericValue = cell.getNumericCellValue();
                // 如果是整数，返回整数
                if (numericValue == Math.floor(numericValue)) {
                    return (long) numericValue;
                }
                return numericValue;
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    try {
                        double formulaValue = cell.getNumericCellValue();
                        if (formulaValue == Math.floor(formulaValue)) {
                            return (long) formulaValue;
                        }
                        return formulaValue;
                    } catch (Exception ex) {
                        return null;
                    }
                }
            default:
                return null;
        }
    }
} 