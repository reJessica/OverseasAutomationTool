// 全局变量
window.currentPage = 0;

// Toast提示
function showToast(message, duration = 3000) {
    const toast = $('#toast');
    toast.find('.toast-body').text(message);
    const bsToast = new bootstrap.Toast(toast, {
        delay: duration
    });
    bsToast.show();
}

// 高亮显示匹配文本
function highlightText(text, searchText) {
    if (!searchText || !text) return text;
    const regex = new RegExp(`(${searchText})`, 'gi');
    return text.toString().replace(regex, '<span class="highlight">$1</span>');
}

// 获取报告列表
function loadReports(page = 0, searchText = '') {
    window.currentPage = page;  // 更新当前页码
    // 获取精确/模糊搜索开关状态
    const exactMatch = document.getElementById('exactMatchSwitch') ? document.getElementById('exactMatchSwitch').checked : true;
    $.ajax({
        url: '/overseas/api/report/list',
        method: 'GET',
        data: {
            page: page,
            size: 10,
            search: searchText,
            exactMatch: exactMatch
        },
        success: function(response) {
            console.log('API响应:', response);
            if (response && response.reports) {
                const reports = response.reports || [];
                const totalPages = response.totalPages || 0;
                window.currentPage = response.currentPage || 0;  // 从响应中更新当前页码
                
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
                        <td><input type="checkbox" class="report-checkbox" data-id="${report.id}"></td>
                        <td>${highlightText(report.reportNo, searchText) || '-'}</td>
                        <td>${highlightText(report.pmno, searchText) || '-'}</td>
                        <td>${highlightText(report.productName, searchText) || '-'}</td>
                        <td>
                            <span class="badge ${(report.status || 'PENDING') === 'PENDING' ? 'bg-warning' : 'bg-success'}">
                                ${(report.status || 'PENDING') === 'PENDING' ? '待处理' : '已处理'}
                            </span>
                        </td>
                        <td>
                            <button class="btn btn-sm btn-info" onclick="viewReportDetail(${report.id})">查看</button>
                            <button class="btn btn-sm btn-danger" onclick="deleteReport(${report.id})">删除</button>
                            <button class="btn btn-sm btn-warning" onclick="updateStatus(${report.id}, '${(report.status || 'PENDING') === 'PENDING' ? 'PROCESSED' : 'PENDING'}')">
                                ${(report.status || 'PENDING') === 'PENDING' ? '已处理' : '待处理'}
                            </button>
                        </td>
                    `;
                    tbody.appendChild(row);
                });
                
                // 更新分页
                updatePagination(page, totalPages);

                // 显示搜索结果提示
                if (searchText) {
                    const resultCount = reports.length;
                    if (resultCount > 0) {
                        // 显示匹配字段信息
                        const matchedFields = [];
                        reports.forEach(report => {
                            if (report.reportNo && report.reportNo.includes(searchText)) matchedFields.push('报告编号');
                            if (report.pmno && report.pmno.includes(searchText)) matchedFields.push('报告编码');
                            if (report.productName && report.productName.includes(searchText)) matchedFields.push('产品名称');
                        });
                        const uniqueFields = [...new Set(matchedFields)];
                        showToast(`搜索完成，找到 ${resultCount} 条记录，匹配字段：${uniqueFields.join('、')}`, 3000);
                    } else {
                        showToast('未找到匹配的记录', 2000);
                    }
                }
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

// 防抖函数
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// 搜索报告
function searchReports() {
    const searchText = document.getElementById('searchInput').value.trim();
    if (searchText.length > 0) {
        showToast('正在搜索...', 1000);
    }
    loadReports(0, searchText);
}

// 查看报告详情
function viewReportDetail(id) {
    window.location.href = `overseas-report-detail.html?id=${id}`;
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
                    loadReports(window.currentPage);  // 使用当前页码重新加载
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
                loadReports(0);  // 直接使用0作为页码重新加载
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
    
    // 使用防抖处理搜索
    const debouncedSearch = debounce(searchReports, 500);
    
    // 绑定搜索按钮点击事件
    document.getElementById('searchButton').addEventListener('click', searchReports);
    
    // 绑定搜索框输入事件（使用防抖）
    document.getElementById('searchInput').addEventListener('input', debouncedSearch);
    
    // 绑定搜索框回车事件
    document.getElementById('searchInput').addEventListener('keyup', function(e) {
        if (e.key === 'Enter') {
            searchReports();
        }
    });
});

// 批量导出按钮点击事件
$(document).on('click', '#batchExportBtn', function() {
    const selectedIds = [];
    $('.report-checkbox:checked').each(function() {
        selectedIds.push($(this).data('id'));
    });

    if (selectedIds.length === 0) {
        showToast('请先选择要导出的报告！');
        return;
    }

    $.ajax({
        url: '/overseas/api/report/batch-export',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(selectedIds),
        xhrFields: {
            responseType: 'blob'
        },
        success: function(blob, status, xhr) {
            let filename = '报告批量导出.xlsx';
            const disposition = xhr.getResponseHeader('Content-Disposition');
            if (disposition && disposition.indexOf('filename=') !== -1) {
                filename = decodeURIComponent(disposition.split('filename=')[1]);
            }
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
        },
        error: function() {
            showToast('导出失败，请重试！');
        }
    });
}); 