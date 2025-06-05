package org.example.controller;

import org.example.service.FileImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/import")
public class FileImportController {
    private static final Logger logger = LoggerFactory.getLogger(FileImportController.class);

    @Autowired
    private FileImportService fileImportService;

    @PostMapping("/csv")
    public ResponseEntity<?> importCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tableName") String tableName) {
        try {
            logger.info("开始处理CSV文件上传, 文件名: {}, 目标表: {}, 文件大小: {} bytes", 
                file.getOriginalFilename(), tableName, file.getSize());

            if (file.isEmpty()) {
                logger.warn("上传的文件为空");
                return ResponseEntity.badRequest().body("请选择文件");
            }
            
            if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                logger.warn("文件类型不正确: {}", file.getOriginalFilename());
                return ResponseEntity.badRequest().body("请上传CSV文件");
            }

            fileImportService.importCsv(file, tableName);
            String successMessage = "CSV数据导入成功！表名：" + tableName;
            logger.info(successMessage);
            return ResponseEntity.ok(successMessage);
        } catch (IllegalArgumentException e) {
            logger.warn("CSV导入参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("CSV导入失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body("导入失败：" + e.getMessage());
        }
    }

    @PostMapping("/excel")
    public ResponseEntity<?> importExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tableName") String tableName) {
        try {
            logger.info("开始处理Excel文件上传, 文件名: {}, 目标表: {}, 文件大小: {} bytes", 
                file.getOriginalFilename(), tableName, file.getSize());

            if (file.isEmpty()) {
                logger.warn("上传的文件为空");
                return ResponseEntity.badRequest().body("请选择文件");
            }
            
            if (!file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
                logger.warn("文件类型不正确: {}", file.getOriginalFilename());
                return ResponseEntity.badRequest().body("请上传XLSX格式的Excel文件");
            }

            fileImportService.importExcel(file, tableName);
            String successMessage = "Excel数据导入成功！表名：" + tableName;
            logger.info(successMessage);
            return ResponseEntity.ok(successMessage);
        } catch (IllegalArgumentException e) {
            logger.warn("Excel导入参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Excel导入失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body("导入失败：" + e.getMessage());
        }
    }
} 