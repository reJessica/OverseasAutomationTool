// 导入报告
function importReport() {
    const fileInput = document.getElementById('importFile');
    const file = fileInput.files[0];
    if (!file) {
        showToast('请选择要导入的文件');
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    $.ajax({
        url: '/overseas/api/report/import',
        method: 'POST',
        data: formData,
        processData: false,
        contentType: false,
        success: function(response) {
            if (response.success) {
                // 填充表单数据
                fillFormData(response.data);
                showToast('导入成功');
            } else {
                showToast('导入失败：' + response.message);
            }
        },
        error: function(xhr) {
            showToast('导入失败：' + (xhr.responseJSON?.error || '未知错误'));
        }
    });
}

// 填充表单数据
function fillFormData(data) {
    // 遍历数据对象，填充表单字段
    Object.keys(data).forEach(key => {
        const input = $(`[name="${key}"]`);
        if (input.length > 0) {
            input.val(data[key]);
        }
    });
}

// 显示提示信息
function showToast(message, type = 'success', details = '') {
    // 创建toast元素
    const toast = document.createElement('div');
    toast.className = `toast-message ${type}`;
    toast.innerHTML = `
        <div class="toast-content">
            <i class="bi ${type === 'success' ? 'bi-check-circle-fill' : type === 'error' ? 'bi-exclamation-circle-fill' : 'bi-info-circle-fill'}"></i>
            <div class="toast-text">
                <div class="toast-message">${message}</div>
                ${details ? `<div class="toast-details">${details}</div>` : ''}
            </div>
        </div>
    `;
    
    // 添加到页面
    document.body.appendChild(toast);
    
    // 添加显示动画
    setTimeout(() => {
        toast.classList.add('show');
    }, 100);
    
    // 5秒后移除
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => {
            toast.remove();
        }, 300);
    }, 5000);
}

// 保存报告
function saveReport() {
    const form = document.getElementById('reportForm');
    const formData = new FormData(form);
    const data = {};
    
    // 将FormData转换为JSON对象，保持下划线格式
    formData.forEach((value, key) => {
        // 移除空值
        if (value !== '') {
            data[key] = value;
        }
    });

    // 确保必填字段存在
    if (!data.report_no) {
        showToast('表单验证失败', 'error', '报告编号不能为空');
        return;
    }

    // 显示保存中的提示
    showToast('正在保存报告', 'info', '请稍候...');

    $.ajax({
        url: '/overseas/api/report/create',
        method: 'POST',
        data: JSON.stringify(data),
        contentType: 'application/json',
        success: function(response) {
            if (response.success) {
                showToast('保存成功', 'success', '报告已保存，即将跳转到列表页面...');
                setTimeout(() => {
                    window.location.href = '/pages/overseas-table.html';
                }, 1500);
            } else {
                showToast('保存失败', 'error', response.message || '未知错误');
            }
        },
        error: function(xhr) {
            const errorMsg = xhr.responseJSON?.message || '服务器错误';
            showToast('保存失败', 'error', errorMsg);
            console.error('保存失败：', xhr.responseJSON);
        }
    });
}

// 页面加载完成后绑定事件
$(document).ready(function() {
    // 绑定表单提交事件
    $('#reportForm').on('submit', function(e) {
        e.preventDefault();
        saveReport();
    });
    
    // 文件导入事件
    $('#importFile').on('change', function() {
        importReport();
    });
}); 