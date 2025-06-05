// 文件类型配置
const FILE_TYPES = {
    CSV: {
        extension: '.csv',
        mimeType: 'text/csv',
        label: 'CSV文件',
        maxSize: 50 * 1024 * 1024 // 50MB
    },
    EXCEL: {
        extension: '.xlsx',
        mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        label: 'Excel文件',
        maxSize: 50 * 1024 * 1024 // 50MB
    }
};

// 处理文件上传
function handleFileUpload(fileType) {
    const fileInput = document.getElementById(`${fileType.toLowerCase()}File`);
    const tableNameInput = document.getElementById(`${fileType.toLowerCase()}TableName`);
    const statusDiv = document.getElementById(`${fileType.toLowerCase()}UploadStatus`);
    const file = fileInput.files[0];
    const tableName = tableNameInput.value.trim();

    if (!file) {
        statusDiv.innerHTML = `<div class="alert alert-danger">请选择${FILE_TYPES[fileType].label}</div>`;
        return;
    }

    if (!tableName) {
        statusDiv.innerHTML = '<div class="alert alert-danger">请选择目标表</div>';
        return;
    }

    // 验证文件类型
    if (!file.name.toLowerCase().endsWith(FILE_TYPES[fileType].extension)) {
        statusDiv.innerHTML = `<div class="alert alert-danger">请上传${FILE_TYPES[fileType].label}</div>`;
        return;
    }

    // 验证文件大小
    if (file.size > FILE_TYPES[fileType].maxSize) {
        statusDiv.innerHTML = `<div class="alert alert-danger">文件大小不能超过${FILE_TYPES[fileType].maxSize / 1024 / 1024}MB</div>`;
        return;
    }

    const formData = new FormData();
    formData.append('file', file);
    formData.append('tableName', tableName);

    statusDiv.innerHTML = '<div class="alert alert-info">正在上传并处理文件...</div>';

    fetch(`/api/import/${fileType.toLowerCase()}`, {
        method: 'POST',
        body: formData,
        signal: AbortSignal.timeout(300000) // 5分钟超时
    })
    .then(async response => {
        if (!response.ok) {
            const text = await response.text();
            throw new Error(text || '上传失败');
        }
        return response.text();
    })
    .then(result => {
        statusDiv.innerHTML = `
            <div class="alert alert-success">
                ${result}
                <div class="mt-2">
                    <a href="data-table.html?table=${encodeURIComponent(tableName)}" class="btn btn-primary btn-sm">
                        查看数据表格
                    </a>
                </div>
            </div>
        `;
        fileInput.value = ''; // 清空文件输入
    })
    .catch(error => {
        console.error('上传失败:', error);
        let errorMessage = '上传失败';
        if (error.name === 'AbortError') {
            errorMessage = '上传超时，请重试';
        } else if (error.message) {
            errorMessage = error.message;
        }
        statusDiv.innerHTML = `<div class="alert alert-danger">${errorMessage}</div>`;
    });
}

// 加载表结构
function loadTableStructure(tableName, fileType) {
    if (!tableName) {
        document.getElementById(`${fileType.toLowerCase()}TableStructure`).innerHTML = '';
        return;
    }

    fetch(`/api/table/${tableName}/structure`)
        .then(response => response.json())
        .then(structure => {
            const structureDiv = document.getElementById(`${fileType.toLowerCase()}TableStructure`);
            let html = '<div class="table-responsive"><table class="table table-sm table-bordered">';
            html += '<thead><tr><th>列名</th><th>类型</th><th>说明</th></tr></thead><tbody>';
            structure.forEach(column => {
                html += `<tr>
                    <td>${column.COLUMN_NAME}</td>
                    <td>${column.DATA_TYPE}</td>
                    <td>${column.COLUMN_COMMENT || ''}</td>
                </tr>`;
            });
            html += '</tbody></table></div>';
            structureDiv.innerHTML = html;
        })
        .catch(error => {
            console.error('加载表结构失败:', error);
            document.getElementById(`${fileType.toLowerCase()}TableStructure`).innerHTML = 
                '<div class="alert alert-danger">加载表结构失败</div>';
        });
}

function loadTables() {
    fetch('/api/tables')
        .then(response => response.json())
        .then(tables => {
            // 去重
            const uniqueTables = Array.from(new Set(tables));
            const excelSelect = document.getElementById('excelTableName');
            const csvSelect = document.getElementById('csvTableName');
            const optionHtml = '<option value="">请选择表</option>' + 
                uniqueTables.map(table => `<option value="${table}">${table}</option>`).join('');
            excelSelect.innerHTML = optionHtml;
            csvSelect.innerHTML = optionHtml;
        })
        .catch(error => {
            console.error('加载表列表失败:', error);
        });
}

// 页面加载完成后初始化事件监听
document.addEventListener('DOMContentLoaded', function() {
    loadTables();

    // 初始化CSV上传
    const csvUploadForm = document.getElementById('csvUploadForm');
    if (csvUploadForm) {
        csvUploadForm.addEventListener('submit', function(e) {
            e.preventDefault();
            handleFileUpload('CSV');
        });
    }

    // 初始化Excel上传
    const excelUploadForm = document.getElementById('excelUploadForm');
    if (excelUploadForm) {
        excelUploadForm.addEventListener('submit', function(e) {
            e.preventDefault();
            handleFileUpload('EXCEL');
        });
    }

    // 监听表名选择变化
    ['CSV', 'EXCEL'].forEach(fileType => {
        const tableNameSelect = document.getElementById(`${fileType.toLowerCase()}TableName`);
        if (tableNameSelect) {
            tableNameSelect.addEventListener('change', function() {
                loadTableStructure(this.value, fileType);
            });
        }
    });
}); 