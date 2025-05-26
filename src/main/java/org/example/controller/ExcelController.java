package org.example.controller;

import org.example.util.ExcelToSqlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    @Autowired
    private ExcelToSqlConverter excelToSqlConverter;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tableName") String tableName) {
        
        try {
            // 创建临时文件
            Path tempFile = Files.createTempFile("upload-", ".xlsx");
            file.transferTo(tempFile.toFile());

            // 转换Excel到SQL并更新数据库
            excelToSqlConverter.convertExcelToSql(tempFile.toString(), tableName);

            // 删除临时文件
            Files.delete(tempFile);

            return ResponseEntity.ok("Excel文件处理成功！");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("文件处理失败：" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("数据库更新失败：" + e.getMessage());
        }
    }
} 