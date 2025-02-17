// 获取按钮和进度条元素
const startButton = document.getElementById('start-button');
const logContainer = document.getElementById('log-container');

// 点击按钮时触发的事件
startButton.addEventListener('click', async () => {
    try {
        // 禁用按钮，防止重复点击
        startButton.disabled = true;
        console.log('服务运行中 请等待');
        // 清空之前的日志数据
        logContainer.innerHTML = '';

        // 调用后端接口启动服务
        const startResponse = await fetch('/automation/start');
        if (startResponse.ok) {
            console.log('服务启动成功');
            const logData = await startResponse.json();
            console.log('服务处理好的数据如下:', logData); // 添加调试信息

            // 显示日志数据
            logData.forEach(row => {
                const rowElement = document.createElement('div');
                row.forEach((cell, index) => {
                    if (index === 1 && cell.startsWith('http')) {
                        // 如果是第二个元素且是链接，创建超链接元素
                        const linkElement = document.createElement('a');
                        linkElement.href = cell;
                        linkElement.textContent = 'link';
                        linkElement.target = '_blank'; // 在新窗口打开链接
                        rowElement.appendChild(linkElement);
                    } else {
                        const cellElement = document.createElement('span');
                        cellElement.textContent = cell + ' ';
                        rowElement.appendChild(cellElement);
                    }
                });
                logContainer.appendChild(rowElement);
            });
        } else {
            console.error('服务启动失败');
            logContainer.textContent = '服务启动失败';
        }

        // 启用按钮
        startButton.disabled = false;
    } catch (error) {
        console.error('请求出错:', error);
        logContainer.textContent = '请求出错，请稍后重试';
        startButton.disabled = false;
    }
});