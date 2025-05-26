// 配置文件处理
let currentConfig = null;
let isEditing = false;

// 初始化事件监听
document.addEventListener('DOMContentLoaded', function() {
    // 导入配置按钮点击事件
    const importButton = document.getElementById('import-config-button');
    const fileInput = document.getElementById('config-file-input');
    const exportButton = document.getElementById('export-config-button');
    const editButton = document.getElementById('edit-config');
    const saveButton = document.getElementById('save-config');
    const cancelButton = document.getElementById('cancel-edit');

    if (importButton) {
        importButton.addEventListener('click', function() {
            fileInput.click();
        });
    }

    if (fileInput) {
        fileInput.addEventListener('change', handleFileSelect);
    }

    if (exportButton) {
        exportButton.addEventListener('click', exportConfig);
    }

    if (editButton) {
        editButton.addEventListener('click', enableEditing);
    }

    if (saveButton) {
        saveButton.addEventListener('click', saveChanges);
    }

    if (cancelButton) {
        cancelButton.addEventListener('click', cancelEditing);
    }

    // 尝试加载已保存的配置
    loadSavedConfig();
});

// 文件选择处理
function handleFileSelect(event) {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.name.toLowerCase().endsWith('.json')) {
        showToast('错误', '请选择JSON格式的文件', 'danger');
        return;
    }

    const reader = new FileReader();
    reader.onload = function(e) {
        try {
            const config = JSON.parse(e.target.result);
            currentConfig = config;
            displayConfig(config);
            localStorage.setItem('overseasConfig', JSON.stringify(config));
            showToast('成功', '配置文件已成功导入', 'success');
        } catch (error) {
            showToast('错误', '无效的JSON格式', 'danger');
        }
    };
    reader.readAsText(file);
}

// 显示配置内容
function displayConfig(config) {
    const container = document.getElementById('config-table-container');
    if (!container) return;

    let html = '<table class="table table-hover"><thead><tr><th>配置项</th><th>值</th></tr></thead><tbody>';
    
    for (const [key, value] of Object.entries(config)) {
        html += `<tr>
            <td>${key}</td>
            <td class="config-value" data-key="${key}">${isEditing ? 
                `<input type="text" class="form-control" value="${value}">` : 
                value}</td>
        </tr>`;
    }

    html += '</tbody></table>';
    container.innerHTML = html;
}

// 启用编辑模式
function enableEditing() {
    isEditing = true;
    document.getElementById('edit-config').style.display = 'none';
    document.getElementById('save-config').style.display = 'inline-block';
    document.getElementById('cancel-edit').style.display = 'inline-block';
    displayConfig(currentConfig);
}

// 保存更改
function saveChanges() {
    const configValues = document.querySelectorAll('.config-value input');
    const newConfig = {...currentConfig};

    configValues.forEach(input => {
        const key = input.parentElement.dataset.key;
        newConfig[key] = input.value;
    });

    currentConfig = newConfig;
    localStorage.setItem('overseasConfig', JSON.stringify(newConfig));
    
    isEditing = false;
    document.getElementById('edit-config').style.display = 'inline-block';
    document.getElementById('save-config').style.display = 'none';
    document.getElementById('cancel-edit').style.display = 'none';
    
    displayConfig(currentConfig);
    showToast('成功', '配置已保存', 'success');
}

// 取消编辑
function cancelEditing() {
    isEditing = false;
    document.getElementById('edit-config').style.display = 'inline-block';
    document.getElementById('save-config').style.display = 'none';
    document.getElementById('cancel-edit').style.display = 'none';
    displayConfig(currentConfig);
}

// 导出配置
function exportConfig() {
    if (!currentConfig) {
        showToast('错误', '没有可导出的配置', 'danger');
        return;
    }

    const blob = new Blob([JSON.stringify(currentConfig, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'overseas_config.json';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

// 加载保存的配置
function loadSavedConfig() {
    const savedConfig = localStorage.getItem('overseasConfig');
    if (savedConfig) {
        try {
            currentConfig = JSON.parse(savedConfig);
            displayConfig(currentConfig);
        } catch (error) {
            console.error('Failed to load saved config:', error);
        }
    }
}

// 显示提示信息
function showToast(title, message, type = 'info') {
    const toastContainer = document.getElementById('toastContainer');
    const toastId = 'toast-' + Date.now();
    
    const toastHtml = `
        <div id="${toastId}" class="toast align-items-center text-white bg-${type} border-0" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body">
                    <strong>${title}</strong>: ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>
    `;
    
    toastContainer.insertAdjacentHTML('beforeend', toastHtml);
    const toastElement = document.getElementById(toastId);
    const toast = new bootstrap.Toast(toastElement, { delay: 3000 });
    toast.show();
    
    toastElement.addEventListener('hidden.bs.toast', function () {
        toastElement.remove();
    });
}