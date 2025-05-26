// 全局变量
let currentConfig = null;
let isEditing = false;

// 初始化事件监听
document.addEventListener('DOMContentLoaded', function() {
    // 配置管理相关按钮
    const importButton = document.getElementById('import-config-button');
    const fileInput = document.getElementById('config-file-input');
    const exportButton = document.getElementById('export-config-button');
    const editButton = document.getElementById('edit-config');
    const saveButton = document.getElementById('save-config');
    const cancelButton = document.getElementById('cancel-edit');
    
    // 服务控制相关按钮
    const startButton = document.getElementById('start-button');
    const logContainer = document.getElementById('log-container');

    // 配置管理事件监听
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

    // 服务控制事件监听
    if (startButton) {
        startButton.addEventListener('click', async () => {
            try {
                // 检查是否有配置
                if (!currentConfig) {
                    showToast('错误', '请先导入或设置配置', 'danger');
                    return;
                }

                // 检查配置是否完整
                if (!currentConfig.templateFilePath || !currentConfig.logFilePath) {
                    showToast('错误', '配置信息不完整，请确保模板文件路径和日志文件路径都已设置', 'danger');
                    return;
                }

                // 禁用按钮，防止重复点击
                startButton.disabled = true;
                addLog('服务运行中，请等待...');

                // 清空之前的日志数据
                logContainer.innerHTML = '';

                // 首先发送配置
                addLog('正在更新配置...');
                const configResponse = await fetch('/automation/config', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(currentConfig)
                });

                if (!configResponse.ok) {
                    const errorText = await configResponse.text();
                    throw new Error(`配置更新失败: ${errorText}`);
                }

                addLog('配置更新成功，正在启动服务...');

                // 调用后端接口启动服务
                const startResponse = await fetch('/automation/start');
                
                // 添加响应调试信息
                console.log('收到响应:', {
                    status: startResponse.status,
                    statusText: startResponse.statusText,
                    headers: Object.fromEntries(startResponse.headers.entries())
                });

                if (startResponse.ok) {
                    addLog('服务启动成功，正在获取响应数据...');
                    const responseData = await startResponse.json();
                    
                    // 检查响应数据类型
                    if (typeof responseData === 'string') {
                        // 如果是字符串，可能是错误消息
                        addLog(responseData);
                        showToast('警告', responseData, 'warning');
                        return;
                    }

                    if (!Array.isArray(responseData)) {
                        addLog('警告：返回的数据格式不是数组');
                        console.warn('非预期的数据格式:', responseData);
                        return;
                    }

                    // 显示日志数据
                    responseData.forEach((row, rowIndex) => {
                        if (!Array.isArray(row)) {
                            const logEntry = document.createElement('div');
                            logEntry.textContent = JSON.stringify(row);
                            logContainer.appendChild(logEntry);
                            return;
                        }

                        const rowElement = document.createElement('div');
                        rowElement.className = 'log-row';
                        
                        row.forEach((cell, index) => {
                            const cellElement = document.createElement('span');
                            cellElement.className = 'log-cell';
                            
                            if (index === 1 && cell && cell.startsWith('http')) {
                                const linkElement = document.createElement('a');
                                linkElement.href = cell;
                                linkElement.textContent = 'link';
                                linkElement.target = '_blank';
                                cellElement.appendChild(linkElement);
                            } else {
                                cellElement.textContent = cell;
                            }
                            
                            rowElement.appendChild(cellElement);
                        });
                        
                        logContainer.appendChild(rowElement);
                    });
                    
                    showToast('成功', '服务执行完成', 'success');
                } else {
                    const errorText = await startResponse.text();
                    const errorMessage = `服务启动失败 (HTTP ${startResponse.status}): ${errorText}`;
                    console.error(errorMessage);
                    addLog(errorMessage);
                    showToast('错误', errorMessage, 'danger');
                }
            } catch (error) {
                const errorMessage = `请求出错: ${error.message}`;
                console.error(errorMessage, error);
                addLog(errorMessage);
                showToast('错误', errorMessage, 'danger');
            } finally {
                // 启用按钮
                startButton.disabled = false;
            }
        });
    }

    // 尝试加载已保存的配置
    loadSavedConfig();
});

// 配置文件处理相关函数
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

function enableEditing() {
    isEditing = true;
    document.getElementById('edit-config').style.display = 'none';
    document.getElementById('save-config').style.display = 'inline-block';
    document.getElementById('cancel-edit').style.display = 'inline-block';
    displayConfig(currentConfig);
}

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

function cancelEditing() {
    isEditing = false;
    document.getElementById('edit-config').style.display = 'inline-block';
    document.getElementById('save-config').style.display = 'none';
    document.getElementById('cancel-edit').style.display = 'none';
    displayConfig(currentConfig);
}

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

// 添加日志的辅助函数
function addLog(message) {
    const logContainer = document.getElementById('log-container');
    const logElement = document.createElement('div');
    logElement.className = 'log-entry';
    logElement.innerHTML = `<span class="log-time">[${new Date().toLocaleTimeString()}]</span> ${message}`;
    logContainer.appendChild(logElement);
    // 滚动到底部
    logContainer.scrollTop = logContainer.scrollHeight;
}

// 通用工具函数
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