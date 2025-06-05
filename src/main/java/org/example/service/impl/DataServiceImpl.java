package org.example.service.impl;

import org.example.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DataServiceImpl implements DataService {

    private static final Logger logger = LoggerFactory.getLogger(DataServiceImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    private String getDatabaseName() {
        // 从URL中提取数据库名称
        String[] parts = dbUrl.split("/");
        String lastPart = parts[parts.length - 1];
        return lastPart.split("\\?")[0];
    }

    @Override
    public List<String> getAllTables() {
        logger.info("获取所有表名");
        String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = DATABASE()";
        return jdbcTemplate.queryForList(sql, String.class);
    }

    @Override
    public List<Map<String, Object>> getTableStructure(String tableName) {
        logger.info("获取表 {} 的结构", tableName);
        String sql = "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE, COLUMN_KEY, COLUMN_COMMENT " +
                    "FROM information_schema.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? " +
                    "ORDER BY ORDINAL_POSITION";
        return jdbcTemplate.queryForList(sql, tableName);
    }

    @Override
    public Map<String, Object> getData(String tableName, int start, int length, String orderColumn, String orderDir, String search) {
        logger.info("获取表 {} 的数据，起始位置: {}, 长度: {}, 排序列: {}, 排序方向: {}, 搜索: {}", 
                   tableName, start, length, orderColumn, orderDir, search);
        
        Map<String, Object> result = new HashMap<>();
        List<Object> params = new ArrayList<>();
        
        // 构建基础查询
        StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName);
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM " + tableName);
        
        // 添加搜索条件
        if (search != null && !search.trim().isEmpty()) {
            String searchCondition = " WHERE " + orderColumn + " LIKE ?";
            sql.append(searchCondition);
            countSql.append(searchCondition);
            params.add("%" + search + "%");
        }
        
        // 添加排序
        if (orderColumn != null && !orderColumn.trim().isEmpty()) {
            sql.append(" ORDER BY ").append(orderColumn).append(" ").append(orderDir);
        }
        
        // 添加分页
        sql.append(" LIMIT ? OFFSET ?");
        params.add(length);
        params.add(start);
        
        // 执行查询
        List<Map<String, Object>> data = jdbcTemplate.queryForList(sql.toString(), params.toArray());
        Long total = jdbcTemplate.queryForObject(countSql.toString(), 
            params.subList(0, params.size() - 2).toArray(), Long.class);
        
        result.put("data", data);
        result.put("recordsTotal", total);
        result.put("recordsFiltered", total);
        
        return result;
    }

    @Override
    public Map<String, Object> getDataById(String tableName, Long id) {
        logger.info("获取表 {} 中ID为 {} 的数据", tableName, id);
        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";
        return jdbcTemplate.queryForMap(sql, id);
    }

    @Override
    public void deleteData(String tableName, Long id) {
        logger.info("删除表 {} 中ID为 {} 的数据", tableName, id);
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
} 