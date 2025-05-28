package org.example.service.impl;

import org.example.service.ExcelService;
import org.example.util.ExcelToSqlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class ExcelServiceImpl implements ExcelService {

    @Autowired
    private ExcelToSqlConverter excelToSqlConverter;

    @Override
    public void importExcel(MultipartFile file, String tableName) throws Exception {
        // 创建临时文件
        Path tempFile = Files.createTempFile("upload-", ".xlsx");
        file.transferTo(tempFile.toFile());

        try {
            // 转换Excel到SQL并更新数据库
            excelToSqlConverter.convertExcelToSql(tempFile.toString(), tableName);
        } finally {
            // 删除临时文件
            Files.delete(tempFile);
        }
    }
} 