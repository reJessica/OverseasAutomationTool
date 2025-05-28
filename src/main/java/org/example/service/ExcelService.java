package org.example.service;

import org.springframework.web.multipart.MultipartFile;

public interface ExcelService {
    void importExcel(MultipartFile file, String tableName) throws Exception;
} 