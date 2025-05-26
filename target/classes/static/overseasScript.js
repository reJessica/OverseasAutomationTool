// 全局变量
let currentConfig = null;
let isEditing = false;
let statusUpdateInterval = null;

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
    const stopButton = document.getElementById('stop-button');
    const refreshButton = document.getElementById('refresh-button');
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

                // 禁用启动按钮，启用停止按钮
                startButton.disabled = true;
                stopButton.disabled = false;
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

                    // 启动状态更新定时器
                    startStatusUpdateInterval();
                    
                    // 处理响应数据
                    if (responseData && Array.isArray(responseData)) {
                        const linksContainer = document.getElementById('report-links-container');
                        // 保留现有链接，不清除
                        
                        responseData.forEach(log => {
                            // 添加到报告链接区域
                            const linkDiv = document.createElement('div');
                            linkDiv.className = 'report-link-item mb-2';
                            linkDiv.innerHTML = `
                                <div class="d-flex align-items-center">
                                    <span class="me-2">报告编号: ${log[0]}</span>
                                    <a href="${log[1]}" target="_blank" class="btn btn-sm btn-outline-primary">
                                        <i class="bi bi-box-arrow-up-right"></i> 查看报告
                                    </a>
                                </div>
                            `;
                            linksContainer.appendChild(linkDiv);
                            
                            // 添加基本日志信息
                            addLog(`开始处理报告: ${log[0]}`);
                        });
                        
                        // 更新处理数据量
                        const currentCount = parseInt(document.getElementById('data-count').textContent || '0');
                        document.getElementById('data-count').textContent = (currentCount + responseData.length).toString();
                    }
                    
                    showToast('成功', '服务启动成功', 'success');
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

    // 停止服务按钮事件监听
    if (stopButton) {
        stopButton.addEventListener('click', async () => {
            try {
                stopButton.disabled = true;
                addLog('正在停止服务...');

                const response = await fetch('/automation/stop');
                if (response.ok) {
                    addLog('服务已停止');
                    showToast('成功', '服务已停止', 'success');
                    // 停止状态更新
                    stopStatusUpdateInterval();
                    // 重置按钮状态
                    startButton.disabled = false;
                    stopButton.disabled = true;
                    // 更新状态显示
                    document.getElementById('service-status').textContent = '未运行';
                    document.getElementById('data-count').textContent = '0';
                } else {
                    const errorText = await response.text();
                    throw new Error(`停止服务失败: ${errorText}`);
                }
            } catch (error) {
                const errorMessage = `停止服务出错: ${error.message}`;
                console.error(errorMessage);
                addLog(errorMessage);
                showToast('错误', errorMessage, 'danger');
                stopButton.disabled = false;
            }
        });
    }

    // 刷新状态按钮事件监听
    if (refreshButton) {
        refreshButton.addEventListener('click', () => {
            updateServiceStatus();
        });
    }

    // 尝试加载已保存的配置
    loadSavedConfig();
});

// 更新服务状态的函数
async function updateServiceStatus() {
    try {
        const response = await fetch('/automation/status');
        if (response.ok) {
            const status = await response.json();
            
            // 更新状态显示
            document.getElementById('service-status').textContent = status.isRunning ? '运行中' : '未运行';
            document.getElementById('data-count').textContent = status.processedCount || '0';
            
            if (status.lastUpdateTime) {
                const lastUpdate = new Date(status.lastUpdateTime).toLocaleString();
                document.getElementById('last-update').textContent = lastUpdate;
            }

            // 更新按钮状态
            const startButton = document.getElementById('start-button');
            const stopButton = document.getElementById('stop-button');
            if (startButton && stopButton) {
                startButton.disabled = status.isRunning;
                stopButton.disabled = !status.isRunning;
            }
        } else {
            console.error('获取状态失败:', response.statusText);
        }
    } catch (error) {
        console.error('更新状态出错:', error);
    }
}

// 启动状态更新定时器
function startStatusUpdateInterval() {
    if (statusUpdateInterval) {
        clearInterval(statusUpdateInterval);
    }
    // 每5秒更新一次状态
    statusUpdateInterval = setInterval(updateServiceStatus, 5000);
    // 立即更新一次状态
    updateServiceStatus();
}

// 停止状态更新定时器
function stopStatusUpdateInterval() {
    if (statusUpdateInterval) {
        clearInterval(statusUpdateInterval);
        statusUpdateInterval = null;
    }
}

// 添加日志的函数
function addLog(message) {
    const logContainer = document.getElementById('log-container');
    if (logContainer) {
        const logEntry = document.createElement('div');
        logEntry.className = 'log-entry';
        logEntry.innerHTML = `
            <span class="log-time">${new Date().toLocaleTimeString()}</span>
            <span class="log-message">${message}</span>
        `;
        logContainer.appendChild(logEntry);
        // 滚动到底部
        logContainer.scrollTop = logContainer.scrollHeight;
    }
}

// 显示提示消息的函数
function showToast(title, message, type) {
    // 使用Bootstrap的Toast组件
    const toastContainer = document.getElementById('toast-container');
    if (!toastContainer) {
        return;
    }

    const toastElement = document.createElement('div');
    toastElement.className = `toast bg-${type}`;
    toastElement.setAttribute('role', 'alert');
    toastElement.setAttribute('aria-live', 'assertive');
    toastElement.setAttribute('aria-atomic', 'true');

    toastElement.innerHTML = `
        <div class="toast-header">
            <strong class="me-auto">${title}</strong>
            <button type="button" class="btn-close" data-bs-dismiss="toast" aria-label="Close"></button>
        </div>
        <div class="toast-body text-white">
            ${message}
        </div>
    `;

    toastContainer.appendChild(toastElement);
    const toast = new bootstrap.Toast(toastElement);
    toast.show();

    // 监听关闭事件，移除元素
    toastElement.addEventListener('hidden.bs.toast', () => {
        toastElement.remove();
    });
}

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