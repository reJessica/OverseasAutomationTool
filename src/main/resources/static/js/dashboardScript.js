// 初始化图表
let dataTrendChart;
let moduleDistributionChart;

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    initializeCharts();
    updateDashboardData();
    // 每30秒更新一次数据
    setInterval(updateDashboardData, 30000);
});

// 初始化图表
function initializeCharts() {
    // 数据处理趋势图表
    const trendCtx = document.getElementById('dataTrendChart').getContext('2d');
    dataTrendChart = new Chart(trendCtx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: '处理数据量',
                data: [],
                borderColor: '#2962ff',
                tension: 0.4,
                fill: false
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });

    // 模块分布图表
    const distributionCtx = document.getElementById('moduleDistributionChart').getContext('2d');
    moduleDistributionChart = new Chart(distributionCtx, {
        type: 'doughnut',
        data: {
            labels: ['Overseas', 'NMPA', 'Local'],
            datasets: [{
                data: [0, 0, 0],
                backgroundColor: ['#2962ff', '#00c853', '#ff6d00']
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom'
                }
            }
        }
    });
}

// 更新仪表板数据
async function updateDashboardData() {
    try {
        // 获取服务状态
        const statusResponse = await fetch('/automation/status');
        if (statusResponse.ok) {
            const status = await statusResponse.json();
            updateStatusDisplay(status);
        }

        // 获取统计数据
        const statsResponse = await fetch('/api/dashboard/stats');
        if (statsResponse.ok) {
            const stats = await statsResponse.json();
            updateStatsDisplay(stats);
        }

        // 获取最近活动
        const activitiesResponse = await fetch('/api/dashboard/activities');
        if (activitiesResponse.ok) {
            const activities = await activitiesResponse.json();
            updateActivitiesList(activities);
        }
    } catch (error) {
        console.error('Error updating dashboard:', error);
    }
}

// 更新状态显示
function updateStatusDisplay(status) {
    document.getElementById('serviceStatus').textContent = status.isRunning ? '运行中' : '未运行';
    document.getElementById('serviceStatus').className = status.isRunning ? 'text-success' : 'text-secondary';
}

// 更新统计数据
function updateStatsDisplay(stats) {
    document.getElementById('totalDataCount').textContent = stats.totalCount || 0;
    document.getElementById('todayProcessed').textContent = stats.todayCount || 0;
    document.getElementById('successRate').textContent = `${stats.successRate || 0}%`;

    // 更新趋势图表
    if (stats.trendData) {
        dataTrendChart.data.labels = stats.trendData.labels;
        dataTrendChart.data.datasets[0].data = stats.trendData.values;
        dataTrendChart.update();
    }

    // 更新模块分布图表
    if (stats.moduleDistribution) {
        moduleDistributionChart.data.datasets[0].data = [
            stats.moduleDistribution.overseas || 0,
            stats.moduleDistribution.nmpa || 0,
            stats.moduleDistribution.local || 0
        ];
        moduleDistributionChart.update();
    }
}

// 更新活动列表
function updateActivitiesList(activities) {
    const tbody = document.getElementById('recentActivities');
    tbody.innerHTML = '';

    activities.forEach(activity => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${formatDate(activity.timestamp)}</td>
            <td>${activity.module}</td>
            <td>${activity.action}</td>
            <td><span class="badge bg-${getStatusBadgeClass(activity.status)}">${activity.status}</span></td>
        `;
        tbody.appendChild(row);
    });
}

// 格式化日期
function formatDate(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// 获取状态徽章样式
function getStatusBadgeClass(status) {
    switch (status.toLowerCase()) {
        case 'success':
            return 'success';
        case 'error':
            return 'danger';
        case 'processing':
            return 'warning';
        default:
            return 'secondary';
    }
} 