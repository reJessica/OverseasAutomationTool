package org.example.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelToSqlConverter {
    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    public void convertExcelToSql(String excelFilePath, String tableName) {
        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            
            // 获取列名
            List<String> columnNames = new ArrayList<>();
            for (Cell cell : headerRow) {
                columnNames.add(cell.getStringCellValue());
            }

            // 构建SQL语句
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO ").append(tableName).append(" (");
            sql.append(String.join(", ", columnNames));
            sql.append(") VALUES (");

            // 准备SQL语句
            String placeholders = String.join(", ", columnNames.stream()
                    .map(col -> "?")
                    .toList());
            sql.append(placeholders).append(")");

            // 连接数据库并执行插入
            try (Connection conn = DriverManager.getConnection(dbUrl, username, password);
                 PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

                // 从第二行开始读取数据
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    for (int j = 0; j < columnNames.size(); j++) {
                        Cell cell = row.getCell(j);
                        if (cell != null) {
                            switch (cell.getCellType()) {
                                case STRING:
                                    pstmt.setString(j + 1, cell.getStringCellValue());
                                    break;
                                case NUMERIC:
                                    if (DateUtil.isCellDateFormatted(cell)) {
                                        pstmt.setDate(j + 1, new java.sql.Date(cell.getDateCellValue().getTime()));
                                    } else {
                                        pstmt.setDouble(j + 1, cell.getNumericCellValue());
                                    }
                                    break;
                                case BOOLEAN:
                                    pstmt.setBoolean(j + 1, cell.getBooleanCellValue());
                                    break;
                                default:
                                    pstmt.setString(j + 1, "");
                            }
                        } else {
                            pstmt.setString(j + 1, null);
                        }
                    }
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("转换Excel到SQL时发生错误: " + e.getMessage());
        }
    }
} 