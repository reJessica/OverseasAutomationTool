// 全局变量
let currentConfig = null;
let isEditing = false;
let statusUpdateInterval = null;
let lastProcessedCount = 0;  // 用于跟踪上次处理的数据量
const MAX_LOGS = 100;  // 最大日志条数

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

    // 恢复配置
    const savedConfig = localStorage.getItem('overseasConfig');
    if (savedConfig) {
        currentConfig = JSON.parse(savedConfig);
        displayConfig(currentConfig);
    }

    // 恢复服务状态和数据
    restoreServiceStatus();
    restoreSessionData();

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
        startButton.addEventListener('click', startService);
    }

    if (stopButton) {
        stopButton.addEventListener('click', stopService);
    }

    if (refreshButton) {
        refreshButton.addEventListener('click', () => {
            updateServiceStatus();
        });
    }

    // 启动状态更新定时器
    startStatusUpdateInterval();
});

// 更新服务状态的函数
async function updateServiceStatus() {
    try {
        const response = await fetch('/automation/status');
        if (response.ok) {
            const status = await response.json();
            
            // 更新状态显示
            const serviceStatus = document.getElementById('service-status');
            if (serviceStatus) {
                serviceStatus.textContent = status.isRunning ? '运行中' : '未运行';
                serviceStatus.className = status.isRunning ? 'status-value text-success' : 'status-value text-secondary';
            }
            
            // 更新数据处理量并添加日志
            const dataCount = document.getElementById('data-count');
            if (dataCount) {
                const currentCount = status.processedCount || 0;
                dataCount.textContent = currentCount;
                
                // 如果数据量发生变化，添加日志
                if (currentCount > lastProcessedCount) {
                    const newRecords = currentCount - lastProcessedCount;
                    addLog(`新处理了 ${newRecords} 条数据，总计: ${currentCount} 条`);
                    lastProcessedCount = currentCount;
                }
            }
            
            // 更新最近更新时间
            if (status.lastUpdateTime) {
                const lastUpdate = new Date(status.lastUpdateTime);
                const now = new Date();
                const timeDiff = Math.floor((now - lastUpdate) / 1000); // 秒数
                
                let timeString;
                if (timeDiff < 60) {
                    timeString = `${timeDiff}秒前`;
                } else if (timeDiff < 3600) {
                    timeString = `${Math.floor(timeDiff / 60)}分钟前`;
                } else {
                    timeString = lastUpdate.toLocaleString();
                }
                
                document.getElementById('last-update').textContent = timeString;
            }

            // 更新按钮状态
            const startButton = document.getElementById('start-button');
            const stopButton = document.getElementById('stop-button');
            if (startButton && stopButton) {
                startButton.disabled = status.isRunning;
                stopButton.disabled = !status.isRunning;
            }

            // 保存状态到localStorage
            localStorage.setItem('serviceStatus', status.isRunning ? 'running' : 'stopped');
            localStorage.setItem('lastProcessedCount', status.processedCount || '0');
            localStorage.setItem('lastUpdateTime', status.lastUpdateTime || '');
        } else {
            console.error('获取状态失败:', response.statusText);
            addLog('获取服务状态失败');
        }
    } catch (error) {
        console.error('更新状态出错:', error);
        addLog(`状态更新失败: ${error.message}`);
    }
}

// 启动状态更新定时器
function startStatusUpdateInterval() {
    if (!statusUpdateInterval) {
        updateServiceStatus(); // 立即更新一次
        statusUpdateInterval = setInterval(updateServiceStatus, 5000); // 每5秒更新一次
    }
}

// 停止状态更新定时器
function stopStatusUpdateInterval() {
    if (statusUpdateInterval) {
        clearInterval(statusUpdateInterval);
        statusUpdateInterval = null;
    }
    lastProcessedCount = 0;
}

// 添加日志的函数
function addLog(message) {
    const logContainer = document.getElementById('log-container');
    if (logContainer) {
        const logEntry = document.createElement('div');
        logEntry.className = 'log-entry';
        
        // 获取当前时间
        const now = new Date();
        const timeString = now.toLocaleTimeString();
        const dateString = now.toLocaleDateString();
        
        // 创建日志内容
        logEntry.innerHTML = `
            <div class="log-header">
                <span class="log-date">${dateString}</span>
                <span class="log-time">${timeString}</span>
            </div>
            <div class="log-content">
                <span class="log-message">${message}</span>
            </div>
        `;
        
        // 添加到容器顶部
        logContainer.insertBefore(logEntry, logContainer.firstChild);
        
        // 如果日志条目超过100条，删除最旧的
        while (logContainer.children.length > 100) {
            logContainer.removeChild(logContainer.lastChild);
        }
        
        // 滚动到顶部（因为新日志在顶部）
        logContainer.scrollTop = 0;

        // 保存到会话存储
        const logs = JSON.parse(sessionStorage.getItem('serviceLogs') || '[]');
        logs.unshift({
            date: dateString,
            time: timeString,
            message: message
        });
        while (logs.length > 100) {
            logs.pop();
        }
        sessionStorage.setItem('serviceLogs', JSON.stringify(logs));
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

// 添加报告链接
function addReportLink(number, url) {
    const linksContainer = document.getElementById('report-links-container');
    if (linksContainer) {
        const linkDiv = document.createElement('div');
        linkDiv.className = 'report-link-item mb-2';
        linkDiv.innerHTML = `
            <div class="d-flex align-items-center">
                <span class="me-2">报告编号: ${number}</span>
                <a href="${url}" target="_blank" class="btn btn-sm btn-outline-primary">
                    <i class="bi bi-box-arrow-up-right"></i> 查看报告
                </a>
            </div>
        `;
        linksContainer.appendChild(linkDiv);

        // 保存到会话存储
        const links = JSON.parse(sessionStorage.getItem('reportLinks') || '[]');
        links.push({ number, url });
        sessionStorage.setItem('reportLinks', JSON.stringify(links));
    }
}

// 恢复会话数据
function restoreSessionData() {
    // 恢复日志
    const savedLogs = JSON.parse(sessionStorage.getItem('serviceLogs') || '[]');
    const logContainer = document.getElementById('log-container');
    if (logContainer && savedLogs.length > 0) {
        logContainer.innerHTML = '';
        savedLogs.forEach(log => {
            const logEntry = document.createElement('div');
            logEntry.className = 'log-entry';
            logEntry.innerHTML = `
                <div class="log-header">
                    <span class="log-date">${log.date}</span>
                    <span class="log-time">${log.time}</span>
                </div>
                <div class="log-content">
                    <span class="log-message">${log.message}</span>
                </div>
            `;
            logContainer.appendChild(logEntry);
        });
    }

    // 恢复报告链接
    const savedLinks = JSON.parse(sessionStorage.getItem('reportLinks') || '[]');
    const linksContainer = document.getElementById('report-links-container');
    if (linksContainer && savedLinks.length > 0) {
        linksContainer.innerHTML = '';
        savedLinks.forEach(link => {
            const linkDiv = document.createElement('div');
            linkDiv.className = 'report-link-item mb-2';
            linkDiv.innerHTML = `
                <div class="d-flex align-items-center">
                    <span class="me-2">报告编号: ${link.number}</span>
                    <a href="${link.url}" target="_blank" class="btn btn-sm btn-outline-primary">
                        <i class="bi bi-box-arrow-up-right"></i> 查看报告
                    </a>
                </div>
            `;
            linksContainer.appendChild(linkDiv);
        });
    }
}

async function startService() {
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
        document.getElementById('start-button').disabled = true;
        document.getElementById('stop-button').disabled = false;

        // 清空之前的数据
        document.getElementById('log-container').innerHTML = '';
        document.getElementById('report-links-container').innerHTML = '';
        sessionStorage.removeItem('serviceLogs');
        sessionStorage.removeItem('reportLinks');
        
        addLog('服务运行中，请等待...');

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
        if (!startResponse.ok) {
            const errorText = await startResponse.text();
            throw new Error(`服务启动失败: ${errorText}`);
        }

        // 保存服务状态
        localStorage.setItem('serviceStatus', 'running');
        
        // 启动状态更新定时器
        startStatusUpdateInterval();

        const result = await startResponse.json();
        if (Array.isArray(result)) {
            result.forEach(log => {
                const [number, url] = log;
                addLog(`处理报告: ${number}`);
                addReportLink(number, url);
            });
        }

        showToast('成功', '服务已启动', 'success');
    } catch (error) {
        console.error('启动服务失败:', error);
        document.getElementById('start-button').disabled = false;
        document.getElementById('stop-button').disabled = true;
        showToast('错误', `启动服务失败: ${error.message}`, 'danger');
        localStorage.setItem('serviceStatus', 'stopped');
    }
}

async function stopService() {
    try {
        const response = await fetch('/automation/stop');
        if (!response.ok) {
            throw new Error(`停止服务失败: ${response.statusText}`);
        }

        document.getElementById('start-button').disabled = false;
        document.getElementById('stop-button').disabled = true;
        addLog('服务已停止');
        showToast('成功', '服务已停止', 'success');

        // 保存服务状态
        localStorage.setItem('serviceStatus', 'stopped');
        
        // 清除状态更新定时器
        if (statusUpdateInterval) {
            clearInterval(statusUpdateInterval);
            statusUpdateInterval = null;
        }
    } catch (error) {
        console.error('停止服务失败:', error);
        showToast('错误', `停止服务失败: ${error.message}`, 'danger');
    }
}

// 恢复服务状态
async function restoreServiceStatus() {
    const savedStatus = localStorage.getItem('serviceStatus');
    if (savedStatus === 'running') {
        // 如果之前服务是运行状态，检查当前实际状态
        try {
            const response = await fetch('/automation/status');
            if (response.ok) {
                const status = await response.json();
                if (status.isRunning) {
                    // 如果服务确实在运行，启动状态更新
                    document.getElementById('start-button').disabled = true;
                    document.getElementById('stop-button').disabled = false;
                    startStatusUpdateInterval();
                } else {
                    // 如果服务实际上没有运行，更新本地存储
                    localStorage.setItem('serviceStatus', 'stopped');
                }
            }
        } catch (error) {
            console.error('恢复服务状态失败:', error);
            localStorage.setItem('serviceStatus', 'stopped');
        }
    }

    // 恢复其他状态
    const savedProcessedCount = localStorage.getItem('lastProcessedCount');
    if (savedProcessedCount) {
        const dataCountElement = document.getElementById('data-count');
        if (dataCountElement) {
            dataCountElement.textContent = savedProcessedCount;
        }
    }

    const savedLastUpdateTime = localStorage.getItem('lastUpdateTime');
    if (savedLastUpdateTime) {
        const lastUpdateElement = document.getElementById('last-update');
        if (lastUpdateElement) {
            const lastUpdate = new Date(savedLastUpdateTime);
            lastUpdateElement.textContent = lastUpdate.toLocaleString();
        }
    }
}