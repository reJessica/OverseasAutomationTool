package org.example.service.impl;

import org.example.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 模拟数据
        stats.put("totalCount", 1000);
        stats.put("todayCount", 150);
        stats.put("successRate", 95);

        // 趋势数据
        Map<String, Object> trendData = new HashMap<>();
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = now.minusDays(i);
            labels.add(date.format(formatter));
            values.add(new Random().nextInt(100));
        }
        
        trendData.put("labels", labels);
        trendData.put("values", values);
        stats.put("trendData", trendData);

        // 模块分布
        Map<String, Integer> moduleDistribution = new HashMap<>();
        moduleDistribution.put("overseas", 500);
        moduleDistribution.put("nmpa", 300);
        moduleDistribution.put("local", 200);
        stats.put("moduleDistribution", moduleDistribution);

        return stats;
    }

    @Override
    public Map<String, Object> getActivities() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> activities = new ArrayList<>();

        // 模拟活动数据
        String[] modules = {"Overseas", "NMPA", "Local"};
        String[] actions = {"数据导入", "数据处理", "报告生成", "配置更新"};
        String[] statuses = {"success", "error", "processing"};

        Random random = new Random();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (int i = 0; i < 10; i++) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("timestamp", LocalDateTime.now().minusMinutes(i * 5).format(formatter));
            activity.put("module", modules[random.nextInt(modules.length)]);
            activity.put("action", actions[random.nextInt(actions.length)]);
            activity.put("status", statuses[random.nextInt(statuses.length)]);
            activities.add(activity);
        }

        result.put("activities", activities);
        return result;
    }
} 