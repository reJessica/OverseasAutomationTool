const importConfigButton = document.getElementById('import-config-button');
const configFileInput = document.getElementById('config-file-input');
const configTableContainer = document.getElementById('config-table-container');

importConfigButton.addEventListener('click', () => {
    configFileInput.click();
});

configFileInput.addEventListener('change', async () => {
    const file = configFileInput.files[0];
    if (file) {
        // 显示文件路径
        const filePathDisplay = document.createElement('p');
        filePathDisplay.textContent = `选择的文件路径：${file.path || file.name}`;
        configTableContainer.innerHTML = '';
        configTableContainer.appendChild(filePathDisplay);

        // 发送配置数据到后端
        try {
            const reader = new FileReader();
            reader.onload = async (e) => {
                try {
                    const configData = JSON.parse(e.target.result);
                    const response = await fetch('/automation/config', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify(configData)
                    });
                    if (response.ok) {
                        console.log('配置数据已成功发送到后端');
                    } else {
                        console.error('发送配置数据到后端失败');
                    }
                } catch (error) {
                    console.error('解析JSON文件失败:', error);
                    alert('解析JSON文件失败，请检查文件格式是否正确');
                }
            };
            reader.readAsText(file);
        } catch (error) {
            console.error('请求出错:', error);
        }
    }
});