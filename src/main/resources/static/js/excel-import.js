function handleFileUpload() {
    const fileInput = document.getElementById('excelFile');
    const tableNameInput = document.getElementById('tableName');
    const file = fileInput.files[0];
    const tableName = tableNameInput.value.trim();

    if (!file) {
        alert('请选择Excel文件');
        return;
    }

    if (!tableName) {
        alert('请输入表名');
        return;
    }

    const formData = new FormData();
    formData.append('file', file);
    formData.append('tableName', tableName);

    fetch('/api/excel/upload', {
        method: 'POST',
        body: formData
    })
    .then(response => response.text())
    .then(result => {
        alert(result);
        if (result.includes('数据导入成功')) {
            // 从结果中提取表名
            const tableName = result.split('表名：')[1];
            // 跳转到数据表格页面，并传递表名参数
            window.location.href = `/pages/data-table.html?table=${encodeURIComponent(tableName)}`;
        }
    })
    .catch(error => {
        alert('上传失败：' + error);
    });
} 