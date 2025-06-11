// Toast提示
function showToast(message) {
    const toast = $('#toast');
    toast.find('.toast-body').text(message);
    const bsToast = new bootstrap.Toast(toast);
    bsToast.show();
}

// 获取报告列表
function loadReports(page = 0, searchText = '') {
    $.ajax({
        url: '/overseas/api/report/list',
        method: 'GET',
        data: {
            page: page,
            size: 10,
            search: searchText
        },
        success: function(response) {
            console.log('API响应:', response);
            if (response && response.reports) {
                const reports = response.reports || [];
                const totalPages = response.totalPages || 0;
                
                console.log('报告数据:', reports);
                
                // 清空表格
                const tbody = document.querySelector('#reportsTable tbody');
                if (!tbody) {
                    console.error('找不到表格tbody元素');
                    return;
                }
                tbody.innerHTML = '';
                
                // 填充数据
                reports.forEach(report => {
                    console.log('处理单条报告:', report);
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td>${report.reportNo || '-'}</td>
                        <td>${report.productName || '-'}</td>
                        <td>${report.status || '-'}</td>
                        <td>
                            <button class="btn btn-sm btn-info" onclick="viewReportDetail(${report.id})">查看</button>
                            <button class="btn btn-sm btn-danger" onclick="deleteReport(${report.id})">删除</button>
                            <button class="btn btn-sm btn-warning" onclick="updateStatus(${report.id}, '${report.status === 'DRAFT' ? 'PUBLISHED' : 'DRAFT'}')">
                                ${report.status === 'DRAFT' ? '发布' : '撤回'}
                            </button>
                        </td>
                    `;
                    tbody.appendChild(row);
                });
                
                // 更新分页
                updatePagination(page, totalPages);
            } else {
                console.error('API响应格式不正确:', response);
                showToast('获取报告列表失败：响应格式不正确');
            }
        },
        error: function(xhr, status, error) {
            console.error('API请求失败:', {xhr, status, error});
            let errorMsg = '未知错误';
            try {
                const response = xhr.responseJSON;
                errorMsg = response?.message || response?.error || error;
            } catch (e) {
                console.error('解析错误响应失败:', e);
            }
            showToast('获取报告列表失败：' + errorMsg);
        }
    });
}

// 搜索报告
function searchReports() {
    const searchText = document.getElementById('searchInput').value;
    loadReports(0, searchText);
}

// 查看报告详情
function viewReportDetail(id) {
    window.location.href = `/overseas/report/detail/${id}`;
}

// 删除报告
function deleteReport(id) {
    if (confirm('确定要删除这份报告吗？')) {
        $.ajax({
            url: '/overseas/api/report/remove/' + id,
            method: 'DELETE',
            success: function(response) {
                if (response.success) {
                    showToast('删除成功');
                    loadReports(currentPage);
                } else {
                    showToast('删除失败：' + response.message);
                }
            },
            error: function(xhr) {
                showToast('删除失败：' + (xhr.responseJSON?.error || '未知错误'));
            }
        });
    }
}

// 更新报告状态
function updateStatus(id, status) {
    $.ajax({
        url: '/overseas/api/report/status/' + id,
        method: 'PUT',
        data: { status: status },
        success: function(response) {
            if (response.success) {
                showToast('状态更新成功');
                loadReports(currentPage);
            } else {
                showToast('状态更新失败：' + response.message);
            }
        },
        error: function(xhr) {
            showToast('状态更新失败：' + (xhr.responseJSON?.error || '未知错误'));
        }
    });
}

// 更新分页
function updatePagination(currentPage, totalPages) {
    const pagination = $('#pagination');
    pagination.empty();
    
    // 上一页
    pagination.append(`
        <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="return loadReports(${currentPage - 1})">上一页</a>
        </li>
    `);
    
    // 页码
    for (let i = 0; i < totalPages; i++) {
        pagination.append(`
            <li class="page-item ${currentPage === i ? 'active' : ''}">
                <a class="page-link" href="#" onclick="return loadReports(${i})">${i + 1}</a>
            </li>
        `);
    }
    
    // 下一页
    pagination.append(`
        <li class="page-item ${currentPage === totalPages - 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="return loadReports(${currentPage + 1})">下一页</a>
        </li>
    `);
}

// 页面加载完成后执行
document.addEventListener('DOMContentLoaded', function() {
    // 加载初始数据
    loadReports();
    
    // 绑定搜索按钮点击事件
    document.getElementById('searchButton').addEventListener('click', searchReports);
    
    // 绑定搜索框回车事件
    document.getElementById('searchInput').addEventListener('keyup', function(e) {
        if (e.key === 'Enter') {
            searchReports();
        }
    });
}); 