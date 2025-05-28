package org.example.controller;

import org.example.service.CsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/csv")
public class CsvController {
    private static final Logger logger = LoggerFactory.getLogger(CsvController.class);

    @Autowired
    private CsvService csvService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCsv(
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

            csvService.importCsv(file, tableName);
            String successMessage = "CSV数据导入成功！表名：" + tableName;
            logger.info(successMessage);
            return ResponseEntity.ok(successMessage);
        } catch (IllegalArgumentException e) {
            logger.warn("CSV导入参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            logger.error("CSV导入失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body("导入失败：" + e.getMessage());
        } catch (Exception e) {
            logger.error("CSV导入发生未知错误: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body("导入失败：系统错误，请联系管理员");
        }
    }
}