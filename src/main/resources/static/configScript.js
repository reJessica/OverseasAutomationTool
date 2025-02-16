const saveConfigBtn = document.getElementById('save-config-btn');
const configForm = document.getElementById('config-form');

saveConfigBtn.addEventListener('click', async () => {
    const templatePath = document.getElementById('template-path').value;
    const logPath = document.getElementById('log-path').value;
    const outputPath = document.getElementById('output-path').value;

    try {
        const response = await fetch('/automation/config', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                templatePath,
                logPath,
                outputPath
            })
        });

        if (response.ok) {
            console.log('配置保存成功');
        } else {
            console.error('配置保存失败');
        }
    } catch (error) {
        console.error('请求出错:', error);
    }
});