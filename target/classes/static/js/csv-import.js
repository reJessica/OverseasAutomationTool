function handleCsvUpload() {
    const fileInput = document.getElementById('csvFile');
    const tableNameInput = document.getElementById('csvTableName');
    const statusDiv = document.getElementById('csvUploadStatus');
    const file = fileInput.files[0];
    const tableName = tableNameInput.value.trim();

    if (!file) {
        statusDiv.innerHTML = '<div class="alert alert-danger">请选择CSV文件</div>';
        return;
    }

    if (!tableName) {
        statusDiv.innerHTML = '<div class="alert alert-danger">请选择目标表</div>';
        return;
    }

    const formData = new FormData();
    formData.append('file', file);
    formData.append('tableName', tableName);

    statusDiv.innerHTML = '<div class="alert alert-info">正在上传并处理文件...</div>';

    fetch('/api/csv/upload', {
        method: 'POST',
        body: formData
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => {
                throw new Error(text || '上传失败');
            });
        }
        return response.text();
    })
    .then(result => {
        if (result.includes('数据导入成功')) {
            statusDiv.innerHTML = `
                <div class="alert alert-success">
                    ${result}
                    <div class="mt-2">
                        <a href="data-table.html?table=${encodeURIComponent(tableName)}" class="btn btn-primary btn-sm">
                            <i class="bi bi-table"></i> 查看数据表格
                        </a>
                    </div>
                </div>
            `;
            fileInput.value = '';
        } else {
            statusDiv.innerHTML = '<div class="alert alert-danger">' + result + '</div>';
        }
    })
    .catch(error => {
        console.error('上传错误:', error);
        statusDiv.innerHTML = '<div class="alert alert-danger">上传失败：' + error.message + '</div>';
    });
}

// 页面加载完成后初始化事件监听
document.addEventListener('DOMContentLoaded', function() {
    const csvUploadForm = document.getElementById('csvUploadForm');
    if (csvUploadForm) {
        csvUploadForm.addEventListener('submit', function(e) {
            e.preventDefault();
            handleCsvUpload();
        });
    }

    // 加载表名选项
    const csvTableNameSelect = document.getElementById('csvTableName');
    if (csvTableNameSelect) {
        fetch('/api/tables')
            .then(response => response.json())
            .then(tables => {
                tables.forEach(table => {
                    const option = document.createElement('option');
                    option.value = table;
                    option.textContent = table;
                    csvTableNameSelect.appendChild(option);
                });
            })
            .catch(error => {
                console.error('加载表名失败:', error);
                document.getElementById('csvUploadStatus').innerHTML = 
                    '<div class="alert alert-danger">加载表名失败</div>';
            });
    }

    // 监听表名选择变化
    if (csvTableNameSelect) {
        csvTableNameSelect.addEventListener('change', function() {
            const tableName = this.value;
            if (tableName) {
                fetch(`/api/table/${tableName}/structure`)
                    .then(response => response.json())
                    .then(structure => {
                        const structureDiv = document.getElementById('csvTableStructure');
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
                        document.getElementById('csvTableStructure').innerHTML = 
                            '<div class="alert alert-danger">加载表结构失败</div>';
                    });
            } else {
                document.getElementById('csvTableStructure').innerHTML = '';
            }
        });
    }
}); 