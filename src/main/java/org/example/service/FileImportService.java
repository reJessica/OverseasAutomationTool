package org.example.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileImportService {
    /**
     * 导入CSV文件到指定表
     * @param file CSV文件
     * @param tableName 目标表名
     * @throws Exception 导入过程中的异常
     */
    void importCsv(MultipartFile file, String tableName) throws Exception;

    /**
     * 导入Excel文件到指定表
     * @param file Excel文件
     * @param tableName 目标表名
     * @throws Exception 导入过程中的异常
     */
    void importExcel(MultipartFile file, String tableName) throws Exception;
} 